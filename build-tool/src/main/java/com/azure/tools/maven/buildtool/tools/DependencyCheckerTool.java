package com.azure.tools.maven.buildtool.tools;

import com.azure.tools.maven.buildtool.models.OutdatedDependency;
import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;
import com.azure.tools.maven.buildtool.util.MavenUtils;
import com.azure.tools.maven.buildtool.util.logging.Logger;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;

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
public class DependencyCheckerTool implements Runnable {
    private static Logger LOGGER = Logger.getInstance();

    private static final String AZURE_SDK_BOM_ARTIFACT_ID = "azure-sdk-bom";
    private static final String COM_MICROSOFT_AZURE_GROUP_ID = "com.microsoft.azure";

    public void run() {
        LOGGER.info("Running Dependency Checker Tool");

        checkForBom();
        checkForAzureSdkDependencyVersions();
        checkForAzureSdkTransitiveDependencyConflicts();
        checkForAzureSdkTrackOneDependencies();
        checkForOutdatedAzureSdkDependencies();
    }

    private void checkForBom() {
        // we are looking for the azure-sdk-bom artifact ID listed as a dependency in the dependency management section
        DependencyManagement depMgmt = AzureSdkMojo.MOJO.getProject().getDependencyManagement();
        Optional<Dependency> bomDependency = Optional.empty();
        if (depMgmt != null) {
            bomDependency = depMgmt.getDependencies().stream()
                    .filter(d -> d.getArtifactId().equals(AZURE_SDK_BOM_ARTIFACT_ID))
                    .findAny();
        }

        if (bomDependency.isPresent()) {
            String latestAvailableBomVersion = MavenUtils.getLatestArtifactVersion("com.azure", "azure-sdk-bom");
            boolean isLatestBomVersion = bomDependency.get().getVersion().equals(latestAvailableBomVersion);
            if (!isLatestBomVersion) {
                failOrError(AzureSdkMojo.MOJO::isFailOnMissingAzureSdkBom, getString("outdatedBomDependency"));
            }
        } else {
            failOrError(AzureSdkMojo.MOJO::isFailOnMissingAzureSdkBom, getString("missingBomDependency"));
        }
    }

    private void checkForAzureSdkDependencyVersions() {
        // TODO
    }

    private void checkForAzureSdkTransitiveDependencyConflicts() {
        // TODO
    }

    private void checkForAzureSdkTrackOneDependencies() {
        // Check direct dependencies first for any 'com.microsoft.azure' group IDs. These are under the users direct
        // control, so they could try to upgrade to a newer 'com.azure' version instead.
        Set<OutdatedDependency> outdatedDirectDependencies = getDirectDependencies().stream()
                .filter(a -> COM_MICROSOFT_AZURE_GROUP_ID.equals(a.getGroupId()))
                .map(AzureDependencyMapping::lookupReplacement)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        // check indirect dependencies too, but filter out any dependencies we've already discovered above
        Set<OutdatedDependency> outdatedTransitiveDependencies = getAllDependencies().stream()
                .filter(d -> COM_MICROSOFT_AZURE_GROUP_ID.equals(d.getGroupId()))
                .map(AzureDependencyMapping::lookupReplacement)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(d -> !outdatedDirectDependencies.contains(d))
                .collect(Collectors.toSet());

        // The report is only concerned with GAV, so we simplify it here
        AzureSdkMojo.MOJO.getReport().setOutdatedDirectDependencies(outdatedDirectDependencies);
        AzureSdkMojo.MOJO.getReport().setOutdatedTransitiveDependencies(outdatedTransitiveDependencies);

        if (!outdatedDirectDependencies.isEmpty()) {
            // convert each track one dependency into actionable guidance
            String message = getString("deprecatedDirectDependency");
            for (OutdatedDependency outdatedDependency : outdatedDirectDependencies) {
                message += "\n    - " + outdatedDependency.getGav() + " --> " + outdatedDependency.getSuggestedReplacementGav();
            }
            failOrError(AzureSdkMojo.MOJO::isFailOnDeprecatedMicrosoftLibraryUsage, message);
        }
        if (!outdatedTransitiveDependencies.isEmpty()) {
            // convert each track one dependency into actionable guidance
            String message = getString("deprecatedIndirectDependency");
            for (OutdatedDependency outdatedDependency : outdatedDirectDependencies) {
                message += "\n    - " + outdatedDependency.getGav();
            }
            failOrError(AzureSdkMojo.MOJO::isFailOnDeprecatedMicrosoftLibraryUsage, message);
        }

    }

    private void checkForOutdatedAzureSdkDependencies() {
        // TODO
    }

    private void failOrError(Supplier<Boolean> condition, String message) {
        // warn about lack of BOM dependency
        if (condition.get()) {
            AzureSdkMojo.MOJO.getReport().addFailureMessage(message);
        } else {
            AzureSdkMojo.MOJO.getReport().addErrorMessage(message);
        }
    }
}
