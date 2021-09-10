package com.azure.tools.maven.buildtool.util;

import com.azure.tools.maven.buildtool.mojo.AzureSdkMojo;
import org.apache.maven.artifact.Artifact;

import java.util.List;
import java.util.Set;

public class MojoUtils {
    private MojoUtils() {
        // no-op
    }

    @SuppressWarnings("unchecked")
    public static Set<Artifact> getDirectDependencies() {
        return AzureSdkMojo.MOJO.getProject().getDependencyArtifacts();
    }

//    @SuppressWarnings("unchecked")
//    public static List<Dependency> getAllDependencies(AzureSdkMojo mojo) {
//        return mojo.getProject().getDependencies();
//    }

    @SuppressWarnings("unchecked")
    public static Set<Artifact> getAllDependencies() {
        return AzureSdkMojo.MOJO.getProject().getArtifacts();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getCompileSourceRoots() {
        return ((List<String>)AzureSdkMojo.MOJO.getProject()
                           .getCompileSourceRoots());
    }

}
