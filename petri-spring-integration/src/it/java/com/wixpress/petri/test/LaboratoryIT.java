package com.wixpress.petri.test;

import com.wixpress.petri.experiments.domain.*;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.wixpress.petri.experiments.domain.ExperimentBuilder.aCopyOf;
import static com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.anExperimentSnapshot;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.aNewlyGeneratedExperimentSpec;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * User: Dalias
 * Date: 8/7/14
 * Time: 6:45 PM
 */


public class LaboratoryIT {

    public static final int SAMPLE_APP_PORT = 9015;
    public static final int PETRI_PORT = 9016;
    public static final String THE_KEY = "THE_KEY";

    private final SampleAppRunner sampleApp = new SampleAppRunner(SAMPLE_APP_PORT);
    private final FakePetriServer petri = new FakePetriServer(PETRI_PORT);
    private Experiment originalExperiment;
    private final DateTime now = new DateTime();

    @Before
    public void startServers() throws Exception {
        petri.start();
        sampleApp.start();
        petri.addSpec(
                aNewlyGeneratedExperimentSpec(THE_KEY).
                        withTestGroups(asList("a", "b")).
                        withScopes(new ScopeDefinition("the scope", false)));

        originalExperiment = petri.addExperiment(
                snapshotWithGroups(new TestGroup(0, 100, "a"), new TestGroup(1, 0, "b")));
    }

    @After
    public void stopServers() throws Exception {
        sampleApp.stop();
        petri.stop();
    }

    private void updateExperimentState(final TestGroup... testGroups) {
        petri.updateExperiment(aCopyOf(originalExperiment).withExperimentSnapshot(
                snapshotWithGroups(testGroups).build()
        ));
    }

    private ExperimentSnapshotBuilder snapshotWithGroups(final TestGroup... testGroups) {
        return anExperimentSnapshot().
                withStartDate(now.minusMinutes(1)).
                withEndDate(now.plusYears(1)).
                withKey(THE_KEY).
                withGroups(asList(testGroups)).
                withOnlyForLoggedInUsers(false);
    }

    @Test
    public void conductingASimpleExperiment() throws Exception {
        String testResult = sampleApp.conductExperiment(THE_KEY, "FALLBACK");
        assertThat(testResult, is("a"));
    }

    @Test
    public void experimentsResultsArePreservedAcrossDifferentRequests() throws Exception {

        // this causes the experiment to be persisted
        sampleApp.conductExperiment(THE_KEY, "FALLBACK");

        // flip the toggle so that group 'b' is now the winning group
        updateExperimentState(new TestGroup(0, 0, "a"), new TestGroup(1, 100, "b"));

        assertThat(sampleApp.conductExperiment(THE_KEY, "FALLBACK"), is("a"));
    }


}
