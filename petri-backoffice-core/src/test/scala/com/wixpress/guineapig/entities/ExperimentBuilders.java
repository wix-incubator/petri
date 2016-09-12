package com.wixpress.guineapig.entities;

import com.google.common.collect.ImmutableList;
import com.natpryce.makeiteasy.Maker;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentBuilder;
import com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import org.joda.time.DateTime;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.experiments.domain.ExperimentBuilder.anExperiment;
import static com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.anExperimentSnapshot;
import static com.wixpress.petri.laboratory.dsl.TestGroupMakers.TEST_GROUPS_WITH_FIRST_ALWAYS_WINNING;

/**
 * @author: talyag
 * @since: 12/23/13
 */
public class ExperimentBuilders {

    private static final String SOME_SCOPE = "someScope";

    public static ExperimentBuilder createActiveEditorExperiment(String key) {
        return aLastUpdatedExperiment()
                .withExperimentSnapshot(
                        commonExperimentSnapshotBuilder(key)
                                .withScopes(ImmutableList.of(SOME_SCOPE))
                                .build()
                );
    }

    public static ExperimentBuilder createActiveEditorExperimentNotFromSpec(String key) {
        return aLastUpdatedExperiment()
                .withExperimentSnapshot(
                        commonExperimentSnapshotBuilder(key)
                                .withScopes(ImmutableList.of(SOME_SCOPE))
                                .withFromSpec(false)
                                .build()
                );
    }

    public static ExperimentBuilder createTerminatedEditorExperiment(String key) {
        return aLastUpdatedExperiment()
                .withExperimentSnapshot(commonExperimentSnapshotBuilder(key)
                                .withScopes(ImmutableList.of(SOME_SCOPE))
                                .withStartDate(new DateTime().minusHours(2))
                                .withEndDate(new DateTime().minusHours(1))
                                .build()
                );
    }

    private static ExperimentSnapshotBuilder commonExperimentSnapshotBuilder(String key) {
        return anExperimentSnapshot()
                .withKey(key)
                .withGroups(TEST_GROUPS_WITH_FIRST_ALWAYS_WINNING)
                .withFeatureToggle(true)
                .withStartDate(new DateTime().minusHours(1))
                .withEndDate(new DateTime().plusHours(1))
                .withOnlyForLoggedInUsers(true);
    }

    private static ExperimentBuilder aLastUpdatedExperiment() {
        return anExperiment()
                .withLastUpdated(new DateTime());
    }

    public static Maker<Experiment> createFuture() {
        return a(ExperimentMakers.Experiment,
                with(ExperimentMakers.startDate, new DateTime().plusHours(1)),
                with(ExperimentMakers.endDate, new DateTime().plusHours(2)));
    }
}
