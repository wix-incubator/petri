package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Function;
import com.wixpress.petri.laboratory.ConductionContext;
import com.wixpress.petri.laboratory.UserInfo;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author alex
 * @since 8/17/11 12:54 PM
 */

@JsonDeserialize(builder = ExperimentBuilder.class)
public class Experiment {
    public static final int NO_ID = 0;

    private final int id;
    private final DateTime lastUpdated;
    private final ExperimentSnapshot experimentSnapshot;

    private Map<Integer, TestGroup> groupsMap = new HashMap<Integer, TestGroup>();
    private final Filter theFilter;

    Experiment(int id, DateTime lastUpdated, ExperimentSnapshot snapshot) {
        this.id = id;
        this.lastUpdated = lastUpdated;
        this.experimentSnapshot = snapshot;
        this.setGroupsMap(experimentSnapshot.groups());
        this.theFilter = new AggregateFilter(getFilters());
    }

    private void setGroupsMap(List<TestGroup> groups) {
        for (TestGroup group : groups)
            addGroup(group);
    }

    private void addGroup(TestGroup g) {
        groupsMap.put(g.getId(), g);
    }

    public int getId() {
        return id;
    }

    public DateTime getLastUpdated() {
        return lastUpdated;
    }

    public ExperimentSnapshot getExperimentSnapshot() {
        return experimentSnapshot;
    }

    @JsonIgnore
    public int getOriginalId() {
        return experimentSnapshot.originalId() != NO_ID ? experimentSnapshot.originalId() : getId();
    }

    @JsonIgnore
    public int getSeed() {
        return experimentSnapshot.linkedId() != NO_ID ? experimentSnapshot.linkedId() : getOriginalId();
    }

    @JsonIgnore
    public String getKey() {
        return experimentSnapshot.key();
    }

    @JsonIgnore
    public Boolean isFromSpec() {
        return experimentSnapshot.isFromSpec();
    }

    @JsonIgnore
    public DateTime getCreationDate() {
        return experimentSnapshot.creationDate();
    }

    @JsonIgnore
    public String getDescription() {
        return experimentSnapshot.description();
    }

    @JsonIgnore
    public String getName() {
        return experimentSnapshot.name();
    }

    @JsonIgnore
    public DateTime getStartDate() {
        return experimentSnapshot.startDate();
    }

    @JsonIgnore
    public DateTime getEndDate() {
        return experimentSnapshot.endDate();
    }

    @JsonIgnore
    public List<TestGroup> getGroups() {
        return experimentSnapshot.groups();
    }

    @JsonIgnore
    public List<Filter> getFilters() {
        return experimentSnapshot.filters();
    }

    @JsonIgnore
    public String getScope() {
        return experimentSnapshot.scope();
    }

    @JsonIgnore
    public boolean isPaused() {
        return experimentSnapshot.isPaused();
    }

    @JsonIgnore
    public String getCreator() {
        return experimentSnapshot.creator();
    }

    @JsonIgnore
    public boolean isToggle() {
        return experimentSnapshot.isFeatureToggle();
    }

    @JsonIgnore
    public boolean isPersistent() {
        return experimentSnapshot.isPersistent();
    }

    @JsonIgnore
    public boolean isOnlyForLoggedInUsers() {
        return experimentSnapshot.isOnlyForLoggedInUsers();
    }

    @JsonIgnore
    public String getComment() {
        return experimentSnapshot.comment();
    }

    @JsonIgnore
    public String getUpdater() {
        return experimentSnapshot.updater();
    }

    private boolean isValid() {
        return experimentSnapshot.isValid();
    }

    public TestGroup getTestGroupByChunk(int chunk) {
        TestGroup currentGrp = null;
        int accumulatedChunk = 0;
        for (TestGroup testGroup : getGroups()) {
            accumulatedChunk += testGroup.getChunk();
            currentGrp = testGroup;
            if (accumulatedChunk > chunk)
                break;
        }
        return currentGrp;
    }

    public TestGroup getTestGroupById(int id) {
        return groupsMap.get(id);
    }

    //TODO - can make this private now (and move its test to the FilterTest)
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        if (!isValid()) {
            throw new InvalidExperiment(this);
        }

        return theFilter.isEligible(eligibilityCriteria);
    }

    // TODO: This can be moved to an ActivationPeriod object with all related members and constants
    public boolean isActiveAt(DateTime now) {
        Interval interval = new Interval(getStartDate(), getEndDate());
        return interval.contains(now);
    }

    @JsonIgnore
    public boolean isTerminated() {
        return (getEndDate().isEqual(getStartDate())) || getEndDate().isBeforeNow();
    }

    public boolean containsValue(String value) {
        for (TestGroup testGroup : getGroups()) {
            if (testGroup.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public Experiment terminateAsOf(DateTime instant, Trigger trigger) {
        DateTime terminateTime = getStartDate().isBefore(instant) ? instant : getStartDate();

        return ExperimentBuilder.aCopyOf(this).
                withExperimentSnapshot(ExperimentSnapshotBuilder.aCopyOf(getExperimentSnapshot()).
                        withEndDate(terminateTime).
                        withTrigger(trigger).
                        build()).
                build();
    }

    public Experiment pause(Trigger trigger) {
        return ExperimentBuilder.aCopyOf(this).
                withExperimentSnapshot(ExperimentSnapshotBuilder.aCopyOf(getExperimentSnapshot()).
                                withPaused(true).
                                withTrigger(trigger).
                                build()
                ).build();
    }

    public Experiment resume(Trigger trigger) {
        return ExperimentBuilder.aCopyOf(this).
                withExperimentSnapshot(ExperimentSnapshotBuilder.aCopyOf(getExperimentSnapshot()).
                                withPaused(false).
                                withTrigger(trigger).
                                build()
                ).build();
    }

    public Experiment openToAllAlwaysReturning(String value) {
        return ExperimentBuilder.aCopyOf(this).
                withExperimentSnapshot(ExperimentSnapshotBuilder.aCopyOf(getExperimentSnapshot()).
                                withFeatureToggle(true).
                                withGroups(copyGroupsButWithWinningValue(value)).
                                withFilters(new ArrayList<Filter>()).
                                build()
                ).build();
    }

    private List<TestGroup> copyGroupsButWithWinningValue(final String winningGroup) {


        return newArrayList(transform(getGroups(), new Function<TestGroup, TestGroup>() {
            @Override
            public TestGroup apply(TestGroup input) {
                return new TestGroup(input.getId(), input.getValue().equals(winningGroup) ? 100 : 0, input.getValue());
            }
        }));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Experiment that = (Experiment) o;

        if (id != that.id) return false;
        if (!lastUpdated.equals(that.lastUpdated)) return false;
        if (!experimentSnapshot.equals(that.experimentSnapshot)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + lastUpdated.hashCode();
        result = 31 * result + experimentSnapshot.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Experiment{" +
                "id=" + id +
                ", lastUpdated=" + lastUpdated +
                ", experimentSnapshot=" + experimentSnapshot +
                '}';
    }

    public Experiment pauseOrTerminateAsOf(DateTime instant, List<Filter> newFilters, Trigger trigger) {
        if (canRelyOnPreviouslyConductedUid(newFilters)) {
            return terminateAsOf(instant, trigger);
        }

        if (!isPersistent()) {
            return terminateAsOf(instant, trigger);
        }

        //TODO - change this method to be 'if (mustRetainExperienceForAnonUsers) then pause' (?)
        return pause(trigger);
    }

    private boolean canRelyOnPreviouslyConductedUid(List<Filter> newFilters) {
        return isOnlyForLoggedInUsers() && noNewUsersFilter(newFilters);
    }

    private boolean noNewUsersFilter(List<Filter> newFilters) {
        return !(tryFind(newFilters, instanceOf(NewUsersFilter.class)).isPresent());
    }

    public Assignment conduct(ConductionContext context, UserInfo userInfo) {
        TestGroup winning = null;
        EligibilityCriteria eligibilityCriteria = new EligibilityCriteria(
                userInfo, context.additionalEligibilityCriteria(), getStartDate());

        if (!isPaused() && isEligible(eligibilityCriteria)) {
            if (isToggle()) {
                winning = getTestGroupByChunk(0);
            } else {
                winning = context.testGroupDrawer(userInfo).drawTestGroup(this);
            }
        }

        return new Assignment(context.biAdditions(), userInfo, winning, this);
    }

    public static class InvalidExperiment extends RuntimeException {
        public InvalidExperiment(Experiment experiment) {
            super("cannot conduct invalid experiment, is your client too old? " + experiment.getFilters());
        }
    }

}
