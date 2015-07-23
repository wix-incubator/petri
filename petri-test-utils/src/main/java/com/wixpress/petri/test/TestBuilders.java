package com.wixpress.petri.test;

import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.petri.SpecDefinition;
import org.joda.time.DateTime;

import java.util.Arrays;

import static com.wixpress.petri.experiments.domain.ExperimentBuilder.aCopyOf;
import static com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.anExperimentSnapshot;
import static java.util.Arrays.asList;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class TestBuilders {
    public static SpecDefinition.ExperimentSpecBuilder abSpecBuilder(String key) {
        return SpecDefinition.ExperimentSpecBuilder.aNewlyGeneratedExperimentSpec(key).
                withTestGroups(asList("a", "b")).
                withScopes(ScopeDefinition.aScopeDefinitionForAllUserTypes("the scope"));
    }

    public static ExperimentSnapshotBuilder experimentWithFirstWinning(String key) {
        DateTime now = new DateTime();
        return anExperimentSnapshot().
                withStartDate(now.minusMinutes(1)).
                withEndDate(now.plusYears(1)).
                withKey(key).
                withGroups(asList(new TestGroup(1, 100, "a"), new TestGroup(2, 0, "b"))).
                withOnlyForLoggedInUsers(false);
    }

    public static ExperimentSnapshotBuilder experimentOnRegisteredWithFirstWinning(String key) {
        return experimentWithFirstWinning(key).
                withOnlyForLoggedInUsers(true);
    }

    public static ExperimentBuilder updateExperimentState(Experiment experiment, final TestGroup... testGroups) {
        return aCopyOf(experiment).
                withExperimentSnapshot(ExperimentSnapshotBuilder.aCopyOf(experiment.getExperimentSnapshot())
                                .withGroups(Arrays.asList(testGroups)).build()
                );
    }
}
