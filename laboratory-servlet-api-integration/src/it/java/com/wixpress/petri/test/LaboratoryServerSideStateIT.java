package com.wixpress.petri.test;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;
import com.wixpress.petri.fakeserver.FakePetriServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static com.wixpress.petri.test.SampleAppRunner.SampleAppRunnerWithServerSideStateOff;
import static com.wixpress.petri.test.SampleAppRunner.SampleAppRunnerWithServerSideStateOn;
import static com.wixpress.petri.test.TestBuilders.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * User: Dalias
 * Date: 8/7/14
 * Time: 6:45 PM
 */


public class LaboratoryServerSideStateIT {

    public static final int SAMPLE_APP_PORT = 9015;
    public static final int PETRI_PORT = 9016;
    public static final String THE_KEY = "THE_KEY";

    private SampleAppRunner sampleApp;
    private final FakePetriServer petri = new FakePetriServer(PETRI_PORT, SAMPLE_APP_PORT);

    @Before
    public void startFakeServer() throws Exception {
        petri.start();
        petri.addSpec(abSpecBuilder(THE_KEY));
    }

    @After
    public void stopFakeServer() throws Exception {
        petri.stop();
    }

    private void waitForAsynchStateUpdateToServer() throws InterruptedException {
        Thread.sleep(100);
    }

    private void twoConsecutiveCallsFromDifferentBrowsersShareResult(boolean shareResult) throws IOException, InterruptedException {
        Experiment experiment = petri.addExperiment(experimentOnRegisteredWithFirstWinning(THE_KEY));

        final UUID uuid = UUID.randomUUID();
        sampleApp.conductExperimentByUser(THE_KEY, "FALLBACK", uuid);
        waitForAsynchStateUpdateToServer();
        // flip the toggle so that group 'b' is now the winning group
        petri.updateExperiment(updateExperimentState(experiment, new TestGroup(1, 0, "a"), new TestGroup(2, 100, "b")));

        String expectedValue = shareResult ? "a" : "b";
        assertThat(sampleApp.conductExperimentByUserWithNoPreviousCookies(THE_KEY, "FALLBACK", uuid), is(expectedValue));
    }

    @Test
    public void experimentsResultsArePreservedAcrossDifferentRequestsForRegisteredUsersEvenWhenNoCookies() throws Exception {
        sampleApp = SampleAppRunnerWithServerSideStateOn(SAMPLE_APP_PORT);
        sampleApp.start();

        twoConsecutiveCallsFromDifferentBrowsersShareResult(true);

        sampleApp.stop();
    }

    @Test
    public void experimentsResultsAreNotPreservedOnServerSideIfConfigIsNotEnabled() throws Exception {
        sampleApp = SampleAppRunnerWithServerSideStateOff(SAMPLE_APP_PORT);
        sampleApp.start();

        twoConsecutiveCallsFromDifferentBrowsersShareResult(false);

        sampleApp.stop();
    }



}
