package com.wixpress.petri.test;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;
import com.wixpress.petri.util.ConductExperimentSummaryMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import static com.wixpress.petri.test.TestBuilders.*;
import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */


public class LaboratoryIT {

    public static final int SAMPLE_APP_PORT = 9015;
    public static final int PETRI_PORT = 9016;
    public static final String THE_KEY = "THE_KEY";

    private final SampleAppRunner sampleApp = new SampleAppRunner(SAMPLE_APP_PORT);
    private final FakePetriServer petri = new FakePetriServer(PETRI_PORT);

    @Before
    public void startServers() throws Exception {
        petri.start();
        sampleApp.start();

        petri.addSpec(abSpecBuilder(THE_KEY));
    }

    @After
    public void stopServers() throws Exception {
        sampleApp.stop();
        petri.stop();
    }

    private void assertConductExperimentReported(Experiment experiment) throws UnknownHostException, InterruptedException {
        sleep(20);
        assertThat(petri.getConductExperimentReport(experiment.getId()),
                contains(ConductExperimentSummaryMatcher.hasSummary(InetAddress.getLocalHost().getHostName(), experiment.getId(), "a", 1l)));
    }

    @Test
    public void conductingASimpleExperiment() throws Exception {
        petri.addExperiment(experimentWithFirstWinning(THE_KEY));
        String testResult = sampleApp.conductExperiment(THE_KEY, "FALLBACK");
        assertThat(testResult, is("a"));
    }

    @Test
    public void experimentsResultsArePreservedAcrossDifferentRequests() throws Exception {
        Experiment experiment = petri.addExperiment(experimentWithFirstWinning(THE_KEY));

        // this causes the experiment to be persisted
        sampleApp.conductExperiment(THE_KEY, "FALLBACK");

        // flip the toggle so that group 'b' is now the winning group
        petri.updateExperiment(updateExperimentState(experiment, new TestGroup(1, 0, "a"), new TestGroup(2, 100, "b")));

        assertThat(sampleApp.conductExperiment(THE_KEY, "FALLBACK"), is("a"));
    }

    @Test
    public void experimentsResultsArePreservedAcrossDifferentRequestsForRegisteredUsers() throws Exception {
        Experiment experiment = petri.addExperiment(experimentOnRegisteredWithFirstWinning(THE_KEY));

        // this causes the experiment to be persisted
        final UUID uuid = UUID.randomUUID();
        sampleApp.conductExperimentByUser(THE_KEY, "FALLBACK", uuid);

        // flip the toggle so that group 'b' is now the winning group
        petri.updateExperiment(updateExperimentState(experiment, new TestGroup(1, 0, "a"), new TestGroup(2, 100, "b")));

        assertThat(sampleApp.conductExperimentByUser(THE_KEY, "FALLBACK", uuid), is("a"));
    }


    @Test
    public void conductsExperimentAndReportIt() throws IOException, InterruptedException {
        Experiment experiment = petri.addExperiment(experimentWithFirstWinning(THE_KEY));
        sampleApp.conductExperiment(THE_KEY, "FALLBACK");
        assertConductExperimentReported(experiment);
    }

}
