package com.wixpress.guineapig.entities.ui;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class UiSpec {
    private String key;
    private final List<String> testGroups;
    private final String owner;
    private final long creationDate;
    private final long lastUpdateDate;
    private final List<String> scopes;
    private final boolean canBeDeleted;
    private final boolean persistent;

    @JsonCreator
    public UiSpec(
            @JsonProperty("key") String key,
            @JsonProperty("testGroups") List<String> testGroups,
            @JsonProperty("owner") String owner,
            @JsonProperty("creationDate") long creationDate,
            @JsonProperty("lastUpdateDate") long lastUpdateDate,
            @JsonProperty("scopes") List<String> scopes,
            @JsonProperty("canBeDeleted") boolean canBeDeleted,
            @JsonProperty("persistent") boolean persistent){
        this.key = key;
        this.testGroups = testGroups;
        this.owner = owner;
        this.creationDate = creationDate;
        this.lastUpdateDate = lastUpdateDate;
        this.scopes = scopes;
        this.canBeDeleted = canBeDeleted;
        this.persistent = persistent;
    }

    public String getKey() {
        return key;
    }

    public List<String> getTestGroups() {
        return testGroups;
    }

    public String getOwner() {
        return owner;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public boolean isCanBeDeleted() {
        return canBeDeleted;
    }

    public boolean isPersistent() {
        return persistent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UiSpec uiSpec = (UiSpec) o;

        if (creationDate != uiSpec.creationDate) return false;
        if (lastUpdateDate != uiSpec.lastUpdateDate) return false;
        if (canBeDeleted != uiSpec.canBeDeleted) return false;
        if (persistent != uiSpec.persistent) return false;
        if (key != null ? !key.equals(uiSpec.key) : uiSpec.key != null) return false;
        if (testGroups != null ? !testGroups.equals(uiSpec.testGroups) : uiSpec.testGroups != null) return false;
        if (owner != null ? !owner.equals(uiSpec.owner) : uiSpec.owner != null) return false;
        return !(scopes != null ? !scopes.equals(uiSpec.scopes) : uiSpec.scopes != null);

    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (testGroups != null ? testGroups.hashCode() : 0);
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (int) (creationDate ^ (creationDate >>> 32));
        result = 31 * result + (int) (lastUpdateDate ^ (lastUpdateDate >>> 32));
        result = 31 * result + (scopes != null ? scopes.hashCode() : 0);
        result = 31 * result + (canBeDeleted ? 1 : 0);
        result = 31 * result + (persistent ? 1 : 0);
        return result;
    }
}
