package com.azure.tools.maven.buildtool.models;

import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;
import com.azure.tools.maven.buildtool.util.AnnotatedMethodCallerResult;
import com.azure.tools.maven.buildtool.util.MojoUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.tools.maven.buildtool.util.MojoUtils.getAllDependencies;

public class Report {

    public static final String AZURE_DEPENDENCY_GROUP = "com.azure";
    private final List<String> warningMessages;
    private final List<String> errorMessages;
    private final List<String> failureMessages;

    private String bom;
    private List<String> azureDependencies;
//    private List<String> consumedServiceMethods;
    private Set<AnnotatedMethodCallerResult> serviceMethodCalls;
    private Set<String> outdatedDirectDependencies;
    private Set<String> outdatedTransitiveDependencies;

    public Report() {
        this.warningMessages = new ArrayList<>();
        this.errorMessages = new ArrayList<>();
        this.failureMessages = new ArrayList<>();
    }

    /**
     * The report is concluded once all the tools are run. The tools are designed to withhold all result until
     * the conclusion of this report, and then it is the duty of this report to provide the result to the user.
     */
    public void conclude(AzureSdkMojo mojo) {
        if (!warningMessages.isEmpty() && mojo.getLog().isWarnEnabled()) {
            warningMessages.forEach(mojo.getLog()::warn);
        }
        if (!errorMessages.isEmpty() && mojo.getLog().isErrorEnabled()) {
            errorMessages.forEach(mojo.getLog()::error);
        }
        this.azureDependencies = getAzureDependencies(mojo);
        this.serviceMethodCalls = getServiceMethodCalls(mojo);
        // we throw a single runtime exception encapsulating all failure messages into one
        if (!failureMessages.isEmpty()) {
            StringBuilder sb = new StringBuilder("Build failure for the following reasons:\n");
            failureMessages.forEach(s -> sb.append(" - " + s + "\n"));
            throw new RuntimeException(sb.toString());
        }
    }

    private Set<AnnotatedMethodCallerResult> getServiceMethodCalls(AzureSdkMojo mojo) {
        // TODO
        return Collections.emptySet();
    }


    private Stream<Path> getAllSourceFiles(AzureSdkMojo mojo, Object sourceRootDir) {
        try {
            return Files.walk(Paths.get(sourceRootDir.toString()))
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toFile().getAbsolutePath().endsWith(".java"));
        } catch (Exception exception) {
            mojo.getLog().info("Exception while visiting files " + exception.getMessage());
            return Stream.empty();
        }
    }

    private List<String> getAzureDependencies(AzureSdkMojo mojo) {
        return getAllDependencies(mojo).stream()
                // this includes Track 2 mgmt libraries, spring libraries and data plane libraries
                .filter(artifact -> artifact.getGroupId().startsWith(AZURE_DEPENDENCY_GROUP))
                .map(MojoUtils::toGAV)
                .collect(Collectors.toList());
    }

    public void addWarningMessage(String message) {
        warningMessages.add(message);
    }

    public void addErrorMessage(String message) {
        errorMessages.add(message);
    }

    public void addFailureMessage(String message) {
        failureMessages.add(message);
    }

    public void setServiceMethodCalls(Set<AnnotatedMethodCallerResult> serviceMethodCalls) {
        this.serviceMethodCalls = serviceMethodCalls;
    }

    public void setOutdatedDirectDependencies(Set<String> outdatedDirectDependencies) {
        this.outdatedDirectDependencies = outdatedDirectDependencies;
    }

    public void setOutdatedTransitiveDependencies(Set<String> outdatedTransitiveDependencies) {
        this.outdatedTransitiveDependencies = outdatedTransitiveDependencies;
    }
}
