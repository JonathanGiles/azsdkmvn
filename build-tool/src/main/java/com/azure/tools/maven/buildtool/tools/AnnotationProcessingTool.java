package com.azure.tools.maven.buildtool.tools;

import com.azure.core.annotation.ServiceMethod;
import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;
import com.azure.tools.maven.buildtool.util.AnnotatedMethodCallerResult;
import com.azure.tools.maven.buildtool.util.AnnotationUtils;
import org.apache.maven.artifact.DefaultArtifact;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
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

    public void run(AzureSdkMojo mojo) {
        mojo.getLog().info("Running Annotation Processing Tool");

        File targetDir = new File(mojo.getProject().getBuild().getDirectory() + "/classes/");

        Stream<Path> paths = Stream.concat(
            Stream.of(targetDir.getAbsolutePath()),
            mojo.getProject().getArtifacts().stream().map(d -> ((DefaultArtifact)d).getFile().getAbsolutePath()))
         .map(str -> Paths.get((String) str));

        // Collect all calls to methods annotated with the Azure SDK @ServiceMethod annotation
        final Set<AnnotatedMethodCallerResult> serviceMethodCallers =
            AnnotationUtils.findCallsToAnnotatedMethod(ServiceMethod.class, paths);

        System.out.println(serviceMethodCallers);
    }
}