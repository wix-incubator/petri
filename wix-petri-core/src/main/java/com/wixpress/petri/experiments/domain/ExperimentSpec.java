package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.wixpress.petri.petri.SpecDefinition;
import org.joda.time.DateTime;

import java.util.List;

/**
 * @author: talyag
 * @since: 9/29/13
 */
@JsonDeserialize(builder = SpecDefinition.ExperimentSpecBuilder.class)
public class ExperimentSpec {

    private final ExperimentSpecSnapshot experimentSpecSnapshot;
    private final DateTime creationDate;
    private final DateTime updateDate;

    public ExperimentSpec(String key, String owner, List<String> testGroups, DateTime creationDate, List<ScopeDefinition> scopes, DateTime updateDate, boolean persistent) {
        experimentSpecSnapshot = new ExperimentSpecSnapshot(key, owner, testGroups, scopes, persistent);
        this.creationDate = creationDate;
        this.updateDate = updateDate;
    }

    @JsonIgnore
    public ExperimentSpecSnapshot getExperimentSpecSnapshot() {
        return experimentSpecSnapshot;
    }

    public String getKey() {
        return experimentSpecSnapshot.getKey();
    }

    public List<String> getTestGroups() {
        return experimentSpecSnapshot.getTestGroups();
    }

    public List<ScopeDefinition> getScopes() {
        return experimentSpecSnapshot.getScopes();
    }

    public DateTime getCreationDate() {
        return creationDate;
    }

    public String getOwner() {
        return experimentSpecSnapshot.getOwner();
    }

    public DateTime getUpdateDate() {
        return updateDate;
    }

    public boolean isPersistent() {
        return experimentSpecSnapshot.isPersistent();
    }

    public ExperimentSpec setCreationDate(DateTime creationDate) {
        return new ExperimentSpec(experimentSpecSnapshot.getKey(), experimentSpecSnapshot.getOwner(), experimentSpecSnapshot.getTestGroups(), creationDate, experimentSpecSnapshot.getScopes(), this.updateDate, experimentSpecSnapshot.isPersistent());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExperimentSpec that = (ExperimentSpec) o;

        if (!creationDate.equals(that.creationDate)) return false;
        if (!updateDate.equals(that.updateDate)) return false;
        if (!experimentSpecSnapshot.equals(that.experimentSpecSnapshot)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = experimentSpecSnapshot.hashCode();
        result = 31 * result + creationDate.hashCode();
        result = 31 * result + updateDate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ExperimentSpec{" +
                experimentSpecSnapshot.toString() +
                ", creationDate=" + creationDate +
                ", updateDate=" + updateDate +
                '}';
    }


}
