package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
//TODO - move to Scala. save a snapshot instance instead of all members.
// aCopyOf wil call buildFrom(snapshot)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperimentSnapshotBuilder {

    private static final DateTime UKNOWN_TIME = new DateTime(0);

    private String key = "";
    private boolean isFromSpec = true;
    private DateTime creationDate = UKNOWN_TIME;
    private String scope = "";
    private List<TestGroup> groups = new ArrayList<>();
    private String description = "";
    private String name = "";
    private DateTime startDate = UKNOWN_TIME;
    private DateTime endDate = UKNOWN_TIME;
    private List<Filter> filters = new ArrayList<>();
    private boolean paused = false;
    private String creator = "";
    private int originalId = Experiment.NO_ID;
    private boolean featureToggle = false;
    private int linkedId = Experiment.NO_ID;
    private boolean persistent = true;
    private Boolean onlyForLoggedInUsers = null;
    private String updater = "";
    private String comment = "";
    private int conductLimit = 0;

    private ExperimentSnapshotBuilder() {
    }

    public static ExperimentSnapshotBuilder anExperimentSnapshot() {
        return new ExperimentSnapshotBuilder();
    }

    public static ExperimentSnapshotBuilder aCopyOf(ExperimentSnapshot snapshot) {
        return anExperimentSnapshot().
                withKey(snapshot.key()).
                withFromSpec(snapshot.isFromSpec()).
                withLinkedId(snapshot.linkedId()).
                withOriginalId(snapshot.originalId()).
                withCreationDate(snapshot.creationDate()).
                withScope(snapshot.scope()).
                withGroups(snapshot.groups()).
                withDescription(snapshot.description()).
                withComment(snapshot.comment()).
                withUpdater(snapshot.updater()).
                withName(snapshot.name()).
                withStartDate(snapshot.startDate()).
                withEndDate(snapshot.endDate()).
                withFilters(snapshot.filters()).
                withPaused(snapshot.isPaused()).
                withCreator(snapshot.creator()).
                withFeatureToggle(snapshot.isFeatureToggle()).
                withPersistent(snapshot.isPersistent()).
                withOnlyForLoggedInUsers(snapshot.isOnlyForLoggedInUsers()).
                withConductLimit(snapshot.conductLimit());
    }

    public ExperimentSnapshotBuilder withConductLimit(int conductLimit) {
        this.conductLimit = conductLimit;
        return this;
    }

    public ExperimentSnapshotBuilder withOriginalId(int originalId) {
        this.originalId = originalId;
        return this;
    }

    public ExperimentSnapshotBuilder withLinkedId(int linkedId) {
        this.linkedId = linkedId;
        return this;
    }

    public ExperimentSnapshotBuilder withKey(String key) {
        this.key = key;
        return this;
    }

    public ExperimentSnapshotBuilder withFromSpec(boolean isFromSpec) {
        this.isFromSpec = isFromSpec;
        return this;
    }

    public ExperimentSnapshotBuilder withCreationDate(DateTime creationDate) {
        this.creationDate = creationDate;
        return this;
    }

    public ExperimentSnapshotBuilder withScope(String scope) {
        this.scope = scope;
        return this;
    }

    public ExperimentSnapshotBuilder withGroups(List<TestGroup> testGroups) {
        this.groups = testGroups;
        return this;
    }

    public ExperimentSnapshotBuilder withDescription(String desc) {
        this.description = desc;
        return this;
    }

    public ExperimentSnapshotBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ExperimentSnapshotBuilder withStartDate(DateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public ExperimentSnapshotBuilder withEndDate(DateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    public ExperimentSnapshotBuilder withFilters(List<Filter> filters) {
        this.filters = filters;
        return this;
    }

    public ExperimentSnapshotBuilder withPaused(boolean paused) {
        this.paused = paused;
        return this;
    }

    public ExperimentSnapshotBuilder withCreator(String creator) {
        this.creator = creator;
        return this;
    }

    public ExperimentSnapshotBuilder withFeatureToggle(boolean featureToggle) {
        this.featureToggle = featureToggle;
        return this;
    }

    public ExperimentSnapshotBuilder withPersistent(boolean persistent) {
        this.persistent = persistent;
        return this;
    }

    public ExperimentSnapshotBuilder withOnlyForLoggedInUsers(boolean onlyForLoggedIn) {
        this.onlyForLoggedInUsers = onlyForLoggedIn;
        return this;
    }

    public ExperimentSnapshotBuilder withComment(String comment) {
        this.comment = comment;
        return this;
    }

    public ExperimentSnapshotBuilder withUpdater(String updater) {
        this.updater = updater;
        return this;
    }

    public ExperimentSnapshotBuilder withTrigger(Trigger trigger) {
        return this.withComment(trigger.text()).withUpdater(trigger.updater());
    }

    public ExperimentSnapshot build() {
        validate();
        return new ExperimentSnapshot(key, isFromSpec, creationDate, description, startDate, endDate, assignIdsIfMissing(groups), scope, paused, name, creator, featureToggle, originalId, linkedId, persistent, filters, onlyForLoggedInUsers, comment, updater, conductLimit);
    }

    private List<TestGroup> assignIdsIfMissing(List<TestGroup> groups) {
        Set<Integer> occupiedIDs = ids(groups);
        int availableID = 1;
        List<TestGroup> result = new ArrayList<>(groups);
        for (TestGroup group : result) {
            if (group.getId() == 0) {
                availableID = nextAvailableID(occupiedIDs, availableID);
                group.setId(availableID++);
            }
        }
        return result;
    }

    private int nextAvailableID(Set<Integer> availableIds, int availableID) {
        while (availableIds.contains(availableID))
            availableID++;
        return availableID;
    }

    private Set<Integer> ids(List<TestGroup> groups) {
        Set<Integer> availableIds = new HashSet<Integer>();
        for (TestGroup group : groups) {
            availableIds.add(group.getId());
        }
        return availableIds;
    }

    private void validate() {
        validateTestGroups();
        validateTimeSpan();
        validateFeatureToggle();
        validateOnlyForLoggedIn();
    }

    private void validateOnlyForLoggedIn() {
        if (onlyForLoggedInUsers == null) {
            throw new IllegalArgumentException("an experiment cannot be created without specifying the onlyForLoggedInUsers field");
        }
    }

    private void validateFeatureToggle() {
        if (featureToggle) {
            for (TestGroup testGroup : this.groups) {
                if (testGroup.getChunk() == 100) {
                    return;
                }
            }
            throw new IllegalArgumentException("an experiment marked as a feature toggle must have 1 winning group (with 100%)");
        }
    }

    private void validateTimeSpan() {
        if (endDate.isBefore(startDate))
            throw new IllegalArgumentException(format("start date [%s] cannot be after end date [%s]", startDate, endDate));
    }

    private void validateTestGroups() {
        int sum = 0;
        for (TestGroup testGroup : this.groups) {
            sum += testGroup.getChunk();
        }
        if (sum != 100) {
            throw new IllegalArgumentException("test groups chunk must add up to exactly 100");
        }
    }


}
