package com.azure.tools.maven.buildtool.models;

import java.util.Objects;

public class OutdatedDependency {
    private final String gav;
    private final String suggestedReplacementGav;

    public OutdatedDependency(final String gav, final String suggestedReplacementGav) {
        this.gav = gav;
        this.suggestedReplacementGav = suggestedReplacementGav;
    }

    public String getGav() {
        return gav;
    }

    public String getSuggestedReplacementGav() {
        return suggestedReplacementGav;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OutdatedDependency that = (OutdatedDependency) o;
        return gav.equals(that.gav);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gav);
    }
}
