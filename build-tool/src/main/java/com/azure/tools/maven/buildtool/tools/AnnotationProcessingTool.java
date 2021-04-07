package com.azure.tools.maven.buildtool.tools;

import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;

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
    }
}