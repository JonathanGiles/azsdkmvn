package com.azure.tools.maven.buildtool.tools;

import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;
import com.azure.tools.maven.buildtool.util.MojoUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.plugin.MojoExecutionException;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.tools.maven.buildtool.util.MojoUtils.*;
import static com.azure.tools.maven.buildtool.util.Utils.*;

/**
 * Performs the following tasks:
 *
 * <ul>
 *   <li>Warnings about missing BOM.</li>
 *   <li>Warnings about explicit dependency versions.</li>
 *   <li>Warnings about dependency clashes between Azure libraries and other dependencies.</li>
 *   <li>Warnings about using track one libraries.</li>
 *   <li>Warnings about out of date track two dependencies (BOM and individual libraries).</li>
 * </ul>
 */
public class DependencyCheckerTool implements Tool {
    private static final String AZURE_SDK_BOM_ARTIFACT_ID = "azure-sdk-bom";
    private static final String COM_MICROSOFT_AZURE_GROUP_ID = "com.microsoft.azure";

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
            // TODO check if it is the latest released version
            boolean isLatestBomVersion = true;
            if (!isLatestBomVersion) {
                failOrError(mojo, mojo::isFailOnMissingAzureSdkBom, getString("outdatedBomDependency"));
            }
        } else {
            failOrError(mojo, mojo::isFailOnMissingAzureSdkBom, getString("missingBomDependency"));
        }
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
