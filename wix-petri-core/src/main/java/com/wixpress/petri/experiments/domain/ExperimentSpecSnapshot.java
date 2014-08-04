package com.wixpress.petri.experiments.domain;

import java.util.List;

public class ExperimentSpecSnapshot {
    private final String key;
    private final String owner;
    private final List<String> testGroups;
    private final List<ScopeDefinition> scopes;
    private final boolean persistent;

    public ExperimentSpecSnapshot(String key, String owner, List<String> testGroups, List<ScopeDefinition> scopes, boolean persistent) {
        this.key = key;
        this.owner = owner;
        this.testGroups = testGroups;
        this.scopes = scopes;
        this.persistent = persistent;
    }

    public String getKey() {
        return key;
    }

    public List<String> getTestGroups() {
        return testGroups;
    }

    public List<ScopeDefinition> getScopes() {
        return scopes;
    }

    public String getOwner() {
        return owner;
    }

    public boolean isPersistent() {
        return persistent;
    }

    @Override
    public String toString() {
        return "ExperimentSpecSnapshot{" +
                "key='" + key + '\'' +
                ", owner='" + owner + '\'' +
                ", testGroups=" + testGroups +
                ", scopes=" + scopes +
                ", persistent=" + persistent +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExperimentSpecSnapshot that = (ExperimentSpecSnapshot) o;

        if (!key.equals(that.key)) return false;
        if (!owner.equals(that.owner)) return false;
        if (!scopes.equals(that.scopes)) return false;
        if (!testGroups.equals(that.testGroups)) return false;
        if (persistent != that.persistent) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + owner.hashCode();
        result = 31 * result + testGroups.hashCode();
        result = 31 * result + scopes.hashCode();
        result = 31 * result + (persistent ? 1 : 0);
        return result;
    }


}