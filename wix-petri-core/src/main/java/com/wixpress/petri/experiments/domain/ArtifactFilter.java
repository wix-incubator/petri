package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ArtifactFilter implements Filter {

    private List<String> artifacts;

    public List<String> getArtifacts() {
        return artifacts;
    }

    @JsonCreator
    public ArtifactFilter(@JsonProperty("artifacts") List<String> artifacts) {
        this.artifacts = artifacts;
    }

    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        return artifacts.contains(eligibilityCriteria.getHost());
    }

    @Override
    public String toString() {
        return "ArtifactsFilter{" +
                "artifacts=" + artifacts +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArtifactFilter that = (ArtifactFilter) o;

        if (artifacts != null ? !artifacts.equals(that.artifacts) : that.artifacts != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return artifacts != null ? artifacts.hashCode() : 0;
    }
}
