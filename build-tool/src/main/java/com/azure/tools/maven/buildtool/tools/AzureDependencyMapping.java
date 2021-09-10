package com.azure.tools.maven.buildtool.tools;

import com.azure.tools.maven.buildtool.models.OutdatedDependency;
import com.azure.tools.maven.buildtool.util.MavenUtils;
import com.azure.tools.maven.buildtool.util.logging.Logger;
import org.apache.maven.artifact.Artifact;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AzureDependencyMapping {
    private static Logger LOGGER = Logger.getInstance();

    private static final String TRACK_ONE_GROUP_ID = "com.microsoft.azure";
    private static final String TRACK_TWO_GROUP_ID = "com.azure";

    // This map is for all com.microsoft.* group IDs, mapping them into their com.azure equivalents
    private static final Map<String, String> TRACK_ONE_REDIRECTS = new HashMap<>();
    static {
        TRACK_ONE_REDIRECTS.put("azure-cosmosdb", "azure-cosmos");
    }

    // This method will look to see if we have any recorded guidance on how to replace the given artifact with something
    // else
    public static Optional<OutdatedDependency> lookupReplacement(Artifact a) {
        String groupId = a.getGroupId();
        String artifactId = a.getArtifactId();

        if (TRACK_ONE_GROUP_ID.equals(groupId)) {
            if (TRACK_ONE_REDIRECTS.containsKey(artifactId)) {
                final String newArtifactId = TRACK_ONE_REDIRECTS.get(artifactId);
                final String newGAV = TRACK_TWO_GROUP_ID + ":" + newArtifactId + ":" + MavenUtils.getLatestArtifactVersion(TRACK_TWO_GROUP_ID, newArtifactId);
                return Optional.of(new OutdatedDependency(MavenUtils.toGAV(a), newGAV));
            } else {
                // we've hit a location where we don't know know if the com.microsoft.azure artifact has a newer
                // replacement...For now we will not give a failure
                return Optional.empty();
            }
        }

        return Optional.empty();
    }
}
