package com.azure.tools.maven.buildtool.models;

import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;
import com.azure.tools.maven.buildtool.util.AnnotatedMethodCallerResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Report {

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
     * The report is concluded once all the tools are run. The tools are designed to  withhold all result until
     * the conclusion of this report, and then it is the duty of this report to provide the result to the user.
     */
    public void conclude(AzureSdkMojo mojo) {
        if (!warningMessages.isEmpty() && mojo.getLog().isWarnEnabled()) {
            warningMessages.forEach(mojo.getLog()::warn);
        }
        if (!errorMessages.isEmpty() && mojo.getLog().isErrorEnabled()) {
            errorMessages.forEach(mojo.getLog()::error);
        }
        // we throw a single runtime exception encapsulating all failure messages into one
        if (!failureMessages.isEmpty()) {
            StringBuilder sb = new StringBuilder("Build failure for the following reasons:\n");
            failureMessages.forEach(s -> sb.append(" - " + s + "\n"));
            throw new RuntimeException(sb.toString());
        }
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
