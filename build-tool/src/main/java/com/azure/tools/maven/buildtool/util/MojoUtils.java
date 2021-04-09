package com.azure.tools.maven.buildtool.util;

import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;

import java.util.List;
import java.util.Set;

public class MojoUtils {
    private MojoUtils() {
        // no-op
    }

    @SuppressWarnings("unchecked")
    public static Set<Artifact> getDirectDependencies(AzureSdkMojo mojo) {
        return mojo.getProject().getDependencyArtifacts();
    }

//    @SuppressWarnings("unchecked")
//    public static List<Dependency> getAllDependencies(AzureSdkMojo mojo) {
//        return mojo.getProject().getDependencies();
//    }

    @SuppressWarnings("unchecked")
    public static Set<Artifact> getAllDependencies(AzureSdkMojo mojo) {
        return mojo.getProject().getArtifacts();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getCompileSourceRoots(AzureSdkMojo mojo) {
        return ((List<String>)mojo.getProject()
                           .getCompileSourceRoots());
    }

    public static String toGAV(Dependency d) {
        return d.getGroupId() + ":" + d.getArtifactId() + ":" + d.getVersion();
    }

    public static String toGAV(Artifact a) {
        return a.getGroupId() + ":" + a.getArtifactId() + ":" + a.getVersion();
    }
}
