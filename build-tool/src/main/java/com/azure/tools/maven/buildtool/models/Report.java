package com.azure.tools.maven.buildtool.models;

import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;
import com.azure.tools.maven.buildtool.util.AnnotatedMethodCallerResult;
import com.azure.tools.maven.buildtool.util.MavenUtils;
import com.azure.tools.maven.buildtool.util.MojoUtils;
import com.azure.tools.maven.buildtool.util.logging.Logger;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.tools.maven.buildtool.util.MojoUtils.getAllDependencies;

public class Report {
    private static final Logger LOGGER = Logger.getInstance();

    public static final String AZURE_DEPENDENCY_GROUP = "com.azure";
    private static final String AZURE_SDK_BOM_ARTIFACT_ID = "azure-sdk-bom";
    private final List<String> warningMessages;
    private final List<String> errorMessages;
    private final List<String> failureMessages;

    private List<String> azureDependencies;
    //    private List<String> consumedServiceMethods;
    private Set<AnnotatedMethodCallerResult> serviceMethodCalls;
    private Set<OutdatedDependency> outdatedDirectDependencies;
    private Set<OutdatedDependency> outdatedTransitiveDependencies;
    private String bomVersion;
    private String jsonReport;

    public Report() {
        this.warningMessages = new ArrayList<>();
        this.errorMessages = new ArrayList<>();
        this.failureMessages = new ArrayList<>();
    }

    /**
     * The report is concluded once all the tools are run. The tools are designed to withhold all result until
     * the conclusion of this report, and then it is the duty of this report to provide the result to the user.
     */
    public void conclude() {
        if (!warningMessages.isEmpty() && LOGGER.isWarnEnabled()) {
            warningMessages.forEach(LOGGER::warn);
        }
        if (!errorMessages.isEmpty() && LOGGER.isErrorEnabled()) {
            errorMessages.forEach(LOGGER::error);
        }
        this.bomVersion = getBomVersion();
        this.azureDependencies = getAzureDependencies();

        createJsonReport();
        // we throw a single runtime exception encapsulating all failure messages into one
        if (!failureMessages.isEmpty()) {
            StringBuilder sb = new StringBuilder("Build failure for the following reasons:\n");
            failureMessages.forEach(s -> sb.append(" - " + s + "\n"));
            throw new RuntimeException(sb.toString());
        }
    }

    private String getBomVersion() {
        DependencyManagement depMgmt = AzureSdkMojo.MOJO.getProject().getDependencyManagement();
        Optional<Dependency> bomDependency = Optional.empty();
        if (depMgmt != null) {
            bomDependency = depMgmt.getDependencies().stream()
                    .filter(d -> d.getArtifactId().equals(AZURE_SDK_BOM_ARTIFACT_ID))
                    .findAny();
        }

        if (bomDependency.isPresent()) {
            return bomDependency.get().getVersion();
        }
        return null;
    }

    private void createJsonReport() {

        try {
            StringWriter writer = new StringWriter();
            JsonGenerator generator = new JsonFactory().createGenerator(writer).useDefaultPrettyPrinter();

            generator.writeStartObject();
            generator.writeStringField("group", AzureSdkMojo.MOJO.getProject().getGroupId());
            generator.writeStringField("artifact", AzureSdkMojo.MOJO.getProject().getArtifactId());
            generator.writeStringField("version", AzureSdkMojo.MOJO.getProject().getVersion());
            generator.writeStringField("name", AzureSdkMojo.MOJO.getProject().getName());
            if (this.bomVersion != null && !this.bomVersion.isEmpty()) {
                generator.writeStringField("bomVersion", this.bomVersion);
            }
            if (this.azureDependencies != null && !this.azureDependencies.isEmpty()) {
                writeArray("azureDependencies", azureDependencies, generator);
            }

//            if (this.outdatedDirectDependencies != null && !this.outdatedDirectDependencies.isEmpty()) {
//                writeArray("outdatedDependencies", outdatedDirectDependencies, generator);
//            }

            if (this.serviceMethodCalls != null && !this.serviceMethodCalls.isEmpty()) {
                writeArray("serviceMethodCalls", serviceMethodCalls
                        .stream()
                        .map(AnnotatedMethodCallerResult::toString)
                        .collect(Collectors.toList()), generator);
            }

            if (!this.errorMessages.isEmpty()) {
                writeArray("errorMessages", errorMessages, generator);
            }

            if (!this.warningMessages.isEmpty()) {
                writeArray("warningMessages", warningMessages, generator);
            }

            if (!this.failureMessages.isEmpty()) {
                writeArray("failureMessages", failureMessages, generator);
            }

            generator.writeEndObject();
            generator.close();
            writer.close();

            this.jsonReport = writer.toString();
            final String reportFileString = AzureSdkMojo.MOJO.getReportFile();
            if (reportFileString != null && !reportFileString.isEmpty()) {
                final File reportFile = new File(reportFileString);
                try (FileWriter fileWriter = new FileWriter(reportFile)) {
                    fileWriter.write(this.jsonReport);
                }
            }
        } catch (IOException exception) {

        }
    }

    private void writeArray(String fieldName, Collection<String> values, JsonGenerator generator) throws IOException {
        generator.writeFieldName(fieldName);
        generator.writeStartArray();
        for (String value : values) {
            generator.writeString(value);
        }
        generator.writeEndArray();
    }

    private List<String> getAzureDependencies() {
        return getAllDependencies().stream()
                // this includes Track 2 mgmt libraries, spring libraries and data plane libraries
                .filter(artifact -> artifact.getGroupId().startsWith(AZURE_DEPENDENCY_GROUP))
                .map(MavenUtils::toGAV)
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

    public void setOutdatedDirectDependencies(Set<OutdatedDependency> outdatedDirectDependencies) {
        this.outdatedDirectDependencies = outdatedDirectDependencies;
    }

    public void setOutdatedTransitiveDependencies(Set<OutdatedDependency> outdatedTransitiveDependencies) {
        this.outdatedTransitiveDependencies = outdatedTransitiveDependencies;
    }
}
