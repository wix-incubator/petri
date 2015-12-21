package com.wixpress.petri.experiments.domain;

import java.util.List;

public class ExperimentSpecSnapshot {
    private final String key;
    private final String owner;
    private final List<String> testGroups;
    private final List<ScopeDefinition> scopes;
    private final boolean persistent;
    private final boolean allowedForBots ;

    public ExperimentSpecSnapshot(String key, String owner, List<String> testGroups, List<ScopeDefinition> scopes, boolean persistent, boolean allowedForBots) {
        this.key = key;
        this.owner = owner;
        this.testGroups = testGroups;
        this.scopes = scopes;
        this.persistent = persistent;
        this.allowedForBots = allowedForBots;
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

    public boolean isAllowedForBots() {
        return allowedForBots;
    }

    @Override
    public String toString() {
        return "ExperimentSpecSnapshot{" +
                "key='" + key + '\'' +
                ", owner='" + owner + '\'' +
                ", testGroups=" + testGroups +
                ", scopes=" + scopes +
                ", persistent=" + persistent +
                ", allowedForBots=" + allowedForBots +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExperimentSpecSnapshot that = (ExperimentSpecSnapshot) o;

        if (persistent != that.persistent) return false;
        if (allowedForBots != that.allowedForBots) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (owner != null ? !owner.equals(that.owner) : that.owner != null) return false;
        if (testGroups != null ? !testGroups.equals(that.testGroups) : that.testGroups != null) return false;
        return !(scopes != null ? !scopes.equals(that.scopes) : that.scopes != null);

    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (testGroups != null ? testGroups.hashCode() : 0);
        result = 31 * result + (scopes != null ? scopes.hashCode() : 0);
        result = 31 * result + (persistent ? 1 : 0);
        result = 31 * result + (allowedForBots ? 1 : 0);
        return result;
    }
}