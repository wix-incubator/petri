package com.wixpress.petri.laboratory.dsl;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.Filter;
import com.wixpress.petri.experiments.domain.TestGroup;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import static com.natpryce.makeiteasy.Property.newProperty;
import static com.wixpress.petri.experiments.domain.ExperimentBuilder.anExperiment;
import static com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.anExperimentSnapshot;

/**
 * @author sagyr
 * @since 8/6/13
 */
public class ExperimentMakers {

    public static final Property<Experiment, String> key = newProperty();
    public static final Property<Experiment, Boolean> fromSpec = newProperty();
    public static final Property<Experiment, Integer> id = newProperty();
    public static final Property<Experiment, Integer> originalId = newProperty();
    public static final Property<Experiment, Integer> linkedId = newProperty();
    public static final Property<Experiment, DateTime> lastUpdated = newProperty();
    public static final Property<Experiment, DateTime> creationDate = newProperty();
    public static final Property<Experiment, String> scope = newProperty();
    //TODO - should be Donor<List<TestGroup>> so we can use asList()
    // (isn't listOf good enough / the same?)
    public static final Property<Experiment, List<TestGroup>> testGroups = newProperty();
    public static final Property<Experiment, String> description = newProperty();
    public static final Property<Experiment, DateTime> startDate = newProperty();
    public static final Property<Experiment, DateTime> endDate = newProperty();
    public static final Property<Experiment, List<Filter>> filters = newProperty();
    public static final Property<Experiment, Boolean> paused = newProperty();
    public static final Property<Experiment, Boolean> featureToggle = newProperty();
    public static final Property<Experiment, String> creator = newProperty();
    public static final Property<Experiment, String> name = newProperty();
    public static final Property<Experiment, Boolean> persistent = newProperty();
    public static final Property<Experiment, Boolean> onlyForLoggedIn = newProperty();
    public static final Property<Experiment, String> comment = newProperty();
    public static final Property<Experiment, String> updater = newProperty();

    public static final DateTime DEFAULT_START_DATE = new DateTime();
    public static final DateTime DEFAULT_END_DATE = DEFAULT_START_DATE.plusYears(1);
    public static final DateTime DEFAULT_CREATION_DATE = new DateTime();
    public static final Instantiator<Experiment> Experiment = new Instantiator<Experiment>() {

        @Override
        public Experiment instantiate(PropertyLookup<Experiment> lookup) {
            return anExperiment().
                    withId(lookup.valueOf(id, com.wixpress.petri.experiments.domain.Experiment.NO_ID)).
                    withLastUpdated(lookup.valueOf(lastUpdated, DEFAULT_CREATION_DATE)).
                    withExperimentSnapshot(
                            anExperimentSnapshot().
                                    withKey(lookup.valueOf(key, "")).
                                    withFromSpec(lookup.valueOf(fromSpec, true)).
                                    withName(lookup.valueOf(name, "")).
                                    withCreationDate(lookup.valueOf(creationDate, DEFAULT_CREATION_DATE)).
                                    withDescription(lookup.valueOf(description, "")).
                                    withStartDate(lookup.valueOf(startDate, DEFAULT_START_DATE)).
                                    withEndDate(lookup.valueOf(endDate, DEFAULT_END_DATE)).
                                    withGroups(lookup.valueOf(testGroups, TestGroupMakers.VALID_TEST_GROUP_LIST)).
                                    withScope(lookup.valueOf(scope, "")).
                                    withFilters(lookup.valueOf(filters, new ArrayList<Filter>())).
                                    withPaused(lookup.valueOf(paused, false)).
                                    withOriginalId(lookup.valueOf(originalId, 0)).
                                    withLinkedId(lookup.valueOf(linkedId, 0)).
                                    withFeatureToggle(lookup.valueOf(featureToggle, false)).
                                    withCreator(lookup.valueOf(creator, "")).
                                    withPersistent(lookup.valueOf(persistent, true)).
                                    withOnlyForLoggedInUsers(lookup.valueOf(onlyForLoggedIn, false)).
                                    withComment(lookup.valueOf(comment, "")).
                                    withUpdater(lookup.valueOf(updater, "")).
                                    build()
                    ).
                    build();
        }
    };


    public static Instantiator<Experiment> copyOf(final Experiment original) {
        return new Instantiator<Experiment>() {

            @Override
            public Experiment instantiate(PropertyLookup<Experiment> lookup) {
                return anExperiment().
                        withId(lookup.valueOf(id, original.getId())).
                        withLastUpdated(lookup.valueOf(lastUpdated, original.getLastUpdated())).
                        withExperimentSnapshot(
                                anExperimentSnapshot().
                                        withKey(lookup.valueOf(key, original.getKey())).
                                        withFromSpec(lookup.valueOf(fromSpec, original.isFromSpec())).
                                        withName(lookup.valueOf(name, original.getName())).
                                        withCreationDate(lookup.valueOf(creationDate, original.getCreationDate())).
                                        withDescription(lookup.valueOf(description, original.getDescription())).
                                        withStartDate(lookup.valueOf(startDate, original.getStartDate())).
                                        withEndDate(lookup.valueOf(endDate, original.getEndDate())).
                                        withGroups(lookup.valueOf(testGroups, original.getGroups())).
                                        withScope(lookup.valueOf(scope, original.getScope())).
                                        withFilters(lookup.valueOf(filters, original.getFilters())).
                                        withPaused(lookup.valueOf(paused, original.isPaused())).
                                        withOriginalId(lookup.valueOf(originalId, original.getExperimentSnapshot().originalId())).
                                        withLinkedId(lookup.valueOf(linkedId, original.getExperimentSnapshot().linkedId())).
                                        withFeatureToggle(lookup.valueOf(featureToggle, original.isToggle())).
                                        withCreator(lookup.valueOf(creator, original.getCreator())).
                                        withPersistent(lookup.valueOf(persistent, original.isPersistent())).
                                        withOnlyForLoggedInUsers(lookup.valueOf(onlyForLoggedIn, original.isOnlyForLoggedInUsers())).
                                        withComment(lookup.valueOf(comment, original.getComment())).
                                        withUpdater(lookup.valueOf(updater, original.getUpdater())).
                                        build()
                        ).build();
            }
        };
    }


}
