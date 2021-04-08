package com.azure.tools.maven.buildtool.tools;

import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;
import com.azure.tools.maven.buildtool.util.AnnotatedMethodCallerResult;
import com.azure.tools.maven.buildtool.util.AnnotationUtils;
import org.apache.maven.artifact.Artifact;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static com.azure.tools.maven.buildtool.util.AnnotationUtils.*;

/**
 * Performs the following tasks:
 *
 * <ul>
 *   <li>Reporting to the user all use of @ServiceMethods.</li>
 *   <li>Reporting on use of @Beta-annotated APIs.</li>
 * </ul>
 */
public class AnnotationProcessingTool implements Tool {

    public static final String CONTEXT_ANNOTATION_SERVICE_METHOD_CALLS = "AnnotationProcessingTool.ServiceMethod";

    @SuppressWarnings("unchecked")
    public void run(AzureSdkMojo mojo) {
        mojo.getLog().info("Running Annotation Processing Tool");

        // We build up a list of packages in the source of the user maven project, so that we only report on the
        // usage of annotation methods from code within these packages
        final Set<String> interestedPackages = new TreeSet<>(Comparator.comparingInt(String::length));
        ((List<String>)mojo.getProject()
            .getCompileSourceRoots())
            .forEach(root -> buildPackageList(root, root, interestedPackages));

        final ClassLoader classLoader = getCompleteClassLoader(getAllPaths(mojo));

        // Collect all calls to methods annotated with the Azure SDK @ServiceMethod annotation
        getAnnotation("com.azure.core.annotation.ServiceMethod", classLoader).ifPresent(annotation -> {
            mojo.getContext().put(CONTEXT_ANNOTATION_SERVICE_METHOD_CALLS,
                findCallsToAnnotatedMethod(annotation, getAllPaths(mojo), interestedPackages, true));
        });

        // TODO include support for scanning @Beta APIs, if we decide to provide that functionality

        System.out.println(mojo.getContext().get(CONTEXT_ANNOTATION_SERVICE_METHOD_CALLS));
    }

    private static Stream<Path> getAllPaths(AzureSdkMojo mojo) {
        // This is the user maven build target directory - we look in here for the compiled source code
        final File targetDir = new File(mojo.getProject().getBuild().getDirectory() + "/classes/");

        // this stream of paths is a stream containing the users maven project compiled class files, as well as all
        // jar file dependencies. We use this to analyse the use of annotations and report back to the user.
        return Stream.concat(
                   Stream.of(targetDir.getAbsolutePath()),
                   mojo.getProject().getArtifacts().stream().map(d -> ((Artifact) d).getFile().getAbsolutePath()))
               .map(str -> Paths.get((String) str));
    }

    private static void buildPackageList(String rootDir, String currentDir, Set<String> packages) {
        final File directory = new File(currentDir);

        final File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (final File file : files) {
            if (file.isFile()) {
                final String path = file.getPath();
                final String packageName = path.substring(rootDir.length() + 1, path.lastIndexOf(File.separator));
                packages.add(packageName.replace(File.separatorChar, '.'));
            } else if (file.isDirectory()) {
                buildPackageList(rootDir, file.getAbsolutePath(), packages);
            }
        }
    }
}