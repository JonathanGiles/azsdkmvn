package com.azure.tools.maven.buildtool.tools;

import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;
import com.azure.tools.maven.buildtool.util.logging.Logger;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static com.azure.tools.maven.buildtool.util.AnnotationUtils.*;
import static com.azure.tools.maven.buildtool.util.MojoUtils.*;

/**
 * Performs the following tasks:
 *
 * <ul>
 *   <li>Reporting to the user all use of @ServiceMethods.</li>
 *   <li>Reporting on use of @Beta-annotated APIs.</li>
 * </ul>
 */
public class AnnotationProcessingTool implements Runnable {
    private static Logger LOGGER = Logger.getInstance();

    public void run() {
        LOGGER.info("Running Annotation Processing Tool");

        // We build up a list of packages in the source of the user maven project, so that we only report on the
        // usage of annotation methods from code within these packages
        final Set<String> interestedPackages = new TreeSet<>(Comparator.comparingInt(String::length));
        getCompileSourceRoots().forEach(root -> buildPackageList(root, root, interestedPackages));

        final ClassLoader classLoader = getCompleteClassLoader(getAllPaths());

        // Collect all calls to methods annotated with the Azure SDK @ServiceMethod annotation
        getAnnotation("com.azure.core.annotation.ServiceMethod", classLoader)
            .map(a -> findCallsToAnnotatedMethod(a, getAllPaths(), interestedPackages, true))
            .ifPresent(AzureSdkMojo.MOJO.getReport()::setServiceMethodCalls);

        // TODO include support for scanning @Beta APIs, if we decide to provide that functionality

    }

    private static Stream<Path> getAllPaths() {
        // This is the user maven build target directory - we look in here for the compiled source code
        final File targetDir = new File(AzureSdkMojo.MOJO.getProject().getBuild().getDirectory() + "/classes/");

        // this stream of paths is a stream containing the users maven project compiled class files, as well as all
        // jar file dependencies. We use this to analyse the use of annotations and report back to the user.
        return Stream.concat(
                   Stream.of(targetDir.getAbsolutePath()),
                   getAllDependencies().stream().map(a -> a.getFile().getAbsolutePath()))
               .map(Paths::get);
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