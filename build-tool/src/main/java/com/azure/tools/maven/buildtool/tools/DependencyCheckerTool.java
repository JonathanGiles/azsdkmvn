package com.azure.tools.maven.buildtool.tools;

import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;
import com.azure.tools.maven.buildtool.util.MojoUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.tools.maven.buildtool.util.MojoUtils.getAllDependencies;
import static com.azure.tools.maven.buildtool.util.MojoUtils.getDirectDependencies;
import static com.azure.tools.maven.buildtool.util.Utils.getString;

/**
 * Performs the following tasks:
 *
 * <ul>
 *   <li>Warnings about missing BOM.</li>
 *   <li>Warnings about not using the latest available version of BOM.</li>
 *   <li>Warnings about explicit dependency versions.</li>
 *   <li>Warnings about dependency clashes between Azure libraries and other dependencies.</li>
 *   <li>Warnings about using track one libraries.</li>
 *   <li>Warnings about out of date track two dependencies (BOM and individual libraries).</li>
 * </ul>
 */
public class DependencyCheckerTool implements Tool {
    private static final String AZURE_SDK_BOM_ARTIFACT_ID = "azure-sdk-bom";
    private static final String COM_MICROSOFT_AZURE_GROUP_ID = "com.microsoft.azure";
    public static final XmlMapper XML_MAPPER = new XmlMapper();

    public void run(AzureSdkMojo mojo) {
        mojo.getLog().info("Running Dependency Checker Tool");

        checkForBom(mojo);
        checkForAzureSdkDependencyVersions(mojo);
        checkForAzureSdkTransitiveDependencyConflicts(mojo);
        checkForAzureSdkTrackOneDependencies(mojo);
        checkForOutdatedAzureSdkDependencies(mojo);
    }

    private void checkForBom(AzureSdkMojo mojo) {
        // we are looking for the azure-sdk-bom artifact ID listed as a dependency in the dependency management section
        DependencyManagement depMgmt = mojo.getProject().getDependencyManagement();
        Optional<Dependency> bomDependency = Optional.empty();
        if (depMgmt != null) {
            bomDependency = depMgmt.getDependencies().stream()
                    .filter(d -> d.getArtifactId().equals(AZURE_SDK_BOM_ARTIFACT_ID))
                    .findAny();
        }

        if (bomDependency.isPresent()) {
            String latestAvailableBomVersion = getLatestArtifactVersion(mojo, "azure-sdk-bom");
            boolean isLatestBomVersion = bomDependency.get().getVersion().equals(latestAvailableBomVersion);
            if (!isLatestBomVersion) {
                failOrError(mojo, mojo::isFailOnMissingAzureSdkBom, getString("outdatedBomDependency"));
            }
        } else {
            failOrError(mojo, mojo::isFailOnMissingAzureSdkBom, getString("missingBomDependency"));
        }
    }

    /**
     * Gets the latest released Azure SDK BOM version from Maven repository.
     * @param mojo The plugin mojo.
     *
     * @return The latest Azure SDK BOM version or {@code null} if an error occurred while retrieving the latest
     * version.
     */
    private String getLatestArtifactVersion(AzureSdkMojo mojo, String artifactId) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL("https://repo1.maven.org/maven2/com/azure/" + artifactId + "/maven-metadata.xml");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/xml");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            if (HttpURLConnection.HTTP_OK == responseCode) {
                InputStream responseStream = connection.getInputStream();
                JsonNode jsonNode = XML_MAPPER.readTree(responseStream);
                mojo.getLog().info("" + jsonNode);
                JsonNode node = jsonNode.get("versioning").get("latest");

                String latestVersion = node.asText();
                mojo.getLog().info("The latest version for SDK BOM is " + latestVersion);
                return latestVersion;
            }
            mojo.getLog().info("Got a non-successful response for  " + artifactId + ": " + responseCode);
        } catch (Exception exception) {
            mojo.getLog().info("Got error getting latest Azure SDK BOM version " + exception.getMessage());
        } finally {
            if (connection != null) {
                // closes the input streams and discards the socket
                connection.disconnect();
            }
        }
        return null;
    }

    private void checkForAzureSdkDependencyVersions(AzureSdkMojo mojo) {
        // TODO
    }

    private void checkForAzureSdkTransitiveDependencyConflicts(AzureSdkMojo mojo) {
        // TODO
    }

    private void checkForAzureSdkTrackOneDependencies(AzureSdkMojo mojo) {
        // Check direct dependencies first for any 'com.microsoft.azure' group IDs. These are under the users direct
        // control, so they could try to upgrade to a newer 'com.azure' version instead.
        Set<String> outdatedDirectDependencies = getDirectDependencies(mojo).stream()
                .filter(a -> COM_MICROSOFT_AZURE_GROUP_ID.equals(a.getGroupId()))
                .map(MojoUtils::toGAV)
                .collect(Collectors.toSet());

        // check indirect dependencies too, but filter out any dependencies we've already discovered above
        Set<String> outdatedTransitiveDependencies = getAllDependencies(mojo).stream()
                .filter(d -> COM_MICROSOFT_AZURE_GROUP_ID.equals(d.getGroupId()))
                .map(MojoUtils::toGAV)
                .filter(d -> !outdatedDirectDependencies.contains(d))
                .collect(Collectors.toSet());

        mojo.getReport().setOutdatedDirectDependencies(outdatedDirectDependencies);
        mojo.getReport().setOutdatedTransitiveDependencies(outdatedTransitiveDependencies);

        if (!outdatedTransitiveDependencies.isEmpty()) {
            failOrError(mojo, mojo::isFailOnDeprecatedMicrosoftLibraryUsage, getString("deprecatedDirectDependency"));
        }
        if (!outdatedTransitiveDependencies.isEmpty()) {
            failOrError(mojo, mojo::isFailOnDeprecatedMicrosoftLibraryUsage, getString("deprecatedIndirectDependency"));
        }
    }

    private void checkForOutdatedAzureSdkDependencies(AzureSdkMojo mojo) {
        // TODO
    }

    private void failOrError(AzureSdkMojo mojo, Supplier<Boolean> condition, String message) {
        // warn about lack of BOM dependency
        if (condition.get()) {
            mojo.getReport().addFailureMessage(message);
        } else {
            mojo.getReport().addErrorMessage(message);
        }
    }
}
