package com.azure.tools.maven.buildtool.mojo;

import com.azure.tools.maven.buildtool.models.Report;
import com.azure.tools.maven.buildtool.tools.Tool;
import com.azure.tools.maven.buildtool.tools.Tools;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.util.HashMap;
import java.util.Map;

@Mojo(name = "run",
    defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
    requiresDependencyCollection = ResolutionScope.RUNTIME,
    requiresDependencyResolution = ResolutionScope.RUNTIME)
public class AzureSdkMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "failOnMissingAzureSdkBom", defaultValue = "true")
    private boolean failOnMissingAzureSdkBom;

    @Parameter(property = "failOnDeprecatedMicrosoftLibraryUsage", defaultValue = "false")
    private boolean failOnDeprecatedMicrosoftLibraryUsage;

    @Parameter(property = "failOnUsingMicrosoftDependencyVersions", defaultValue = "false")
    private boolean failOnUsingMicrosoftDependencyVersions;

    @Parameter(property = "sendToMicrosoft", defaultValue = "true", required = false)
    private boolean sendToMicrosoft;

    @Parameter(property = "failOnBeta", defaultValue = "true", required = false)
    private boolean failOnBeta;

//    @Parameter(property = "printToConsole", defaultValue = "true", required = false)
//    private boolean printToConsole;
//
//    @Parameter(property = "writeToFile", defaultValue = "", required = false)
//    private String reportFile;

    private final Report report;

    public AzureSdkMojo() {
        this.report = new Report();
    }

    public Report getReport() {
        return report;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("========================================================================");
        getLog().info("= Running the Azure SDK Maven Build Tool                               =");
        getLog().info("========================================================================");

        // Run all of the tools. They will collect their results in the report.
        Tools.getTools().forEach(t -> t.run(this));

        report.conclude(this);
    }

    public MavenProject getProject() {
        return project;
    }

    public boolean isFailOnMissingAzureSdkBom() {
        return failOnMissingAzureSdkBom;
    }

    public boolean isFailOnDeprecatedMicrosoftLibraryUsage() {
        return failOnDeprecatedMicrosoftLibraryUsage;
    }

    public boolean isFailOnUsingMicrosoftDependencyVersions() {
        return failOnUsingMicrosoftDependencyVersions;
    }

    public boolean isSendToMicrosoft() {
        return sendToMicrosoft;
    }

    public boolean isFailOnBeta() {
        return failOnBeta;
    }

//    public boolean isPrintToConsole() {
//        return printToConsole;
//    }
//
//    public String getReportFile() {
//        return reportFile;
//    }
}
