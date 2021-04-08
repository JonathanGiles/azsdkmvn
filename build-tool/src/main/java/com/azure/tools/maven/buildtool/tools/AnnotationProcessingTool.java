package com.azure.tools.maven.buildtool.tools;

import com.azure.core.annotation.ServiceMethod;
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

        // This is the user maven build target directory - we look in here for the compiled source code
        final File targetDir = new File(mojo.getProject().getBuild().getDirectory() + "/classes/");

        // We build up a list of packages in the source of the user maven project, so that we only report on the
        // usage of annotation methods from code within these packages
        final Set<String> interestedPackages = new TreeSet<>(Comparator.comparingInt(String::length));
        ((List<String>)mojo.getProject()
            .getCompileSourceRoots())
            .forEach(root -> buildPackageList(root, root, interestedPackages));

        // this stream of paths is a stream containing the users maven project compiled class files, as well as all
        // jar file dependencies. We use this to analyse the use of annotations and report back to the user.
        final Stream<Path> paths = Stream.concat(
            Stream.of(targetDir.getAbsolutePath()),
            mojo.getProject().getArtifacts().stream().map(d -> ((Artifact) d).getFile().getAbsolutePath()))
         .map(str -> Paths.get((String) str));

        // Collect all calls to methods annotated with the Azure SDK @ServiceMethod annotation
        final Set<AnnotatedMethodCallerResult> serviceMethodCallers =
            AnnotationUtils.findCallsToAnnotatedMethod(ServiceMethod.class, paths, interestedPackages, true);

        mojo.getContext().put(CONTEXT_ANNOTATION_SERVICE_METHOD_CALLS, serviceMethodCallers);
        serviceMethodCallers.forEach(System.out::println);
    }

    public static void buildPackageList(String rootDir, String currentDir, Set<String> packages) {
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