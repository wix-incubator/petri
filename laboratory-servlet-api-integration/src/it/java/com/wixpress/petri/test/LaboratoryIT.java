package com.wixpress.petri.test;

import com.wix.hoopoe.koboshi.it.RemoteDataFetcherDriver;
import com.wixpress.petri.NonSerializableServerException;
import com.wixpress.petri.PetriRPCClient;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.fakeserver.FakePetriServer;
import com.wixpress.petri.util.ConductExperimentSummaryMatcher;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import specs.valid.ValidStubSpecDefinition_1;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.wixpress.petri.test.TestBuilders.*;
import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.contains;
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
    private final RemoteDataFetcherDriver remoteDataFetcherDriver = RemoteDataFetcherDriver.apply("localhost",SAMPLE_APP_PORT);
    @Before
    public void startServers() throws Exception {
        petri.start();
        sampleApp.start();
    }

    @After
    public void stopServers() throws Exception {
        sampleApp.stop();
        petri.stop();
    }

    //TODO I don't like that it's on top, aligning with current code
    private Experiment setupExperiment(final ExperimentSnapshotBuilder experimentBuilder) {
        petri.addSpec(abSpecBuilder(THE_KEY));
        final Experiment experiment = petri.addExperiment(experimentBuilder);
        remoteDataFetcherDriver.fetch(ConductibleExperiments.class);
        return experiment;
    }

    private void assertConductExperimentReported(Experiment experiment) throws UnknownHostException, InterruptedException {
        sleep(10000);
        System.out.println(petri.getConductExperimentReport(experiment.getId()));
        assertThat(petri.getConductExperimentReport(experiment.getId()),
                contains(ConductExperimentSummaryMatcher.hasSummary(InetAddress.getLocalHost().getHostName(), experiment.getId(), "a", 1l)));
    }

    @Test(expected = NonSerializableServerException.class)
    public void throwsSpecialExceptionIfServerExceptionIsNotSerializable() throws MalformedURLException {
        petri.failNextReuqest();
        PetriRPCClient.makeFor("http://localhost:" + PETRI_PORT + "/petri").fetchActiveExperiments();
    }

    @Test
    public void conductingASimpleExperiment() throws Exception {
        setupExperiment(experimentWithFirstWinning(THE_KEY));
        assertThat(sampleApp.conductExperiment(THE_KEY, "FALLBACK"), is("a"));
    }

    @Test
    public void experimentsResultsArePreservedAcrossDifferentRequests() throws Exception {
        final Experiment experiment = setupExperiment(experimentWithFirstWinning(THE_KEY));

        // this causes the experiment to be persisted
        sampleApp.conductExperiment(THE_KEY, "FALLBACK");

        // flip the toggle so that group 'b' is now the winning group
        petri.updateExperiment(updateExperimentState(experiment, new TestGroup(1, 0, "a"), new TestGroup(2, 100, "b")));

        assertThat(sampleApp.conductExperiment(THE_KEY, "FALLBACK"), is("a"));
    }

    @Test
    public void experimentsResultsArePreservedAcrossDifferentRequestsForRegisteredUsers() throws Exception {
        final Experiment experiment = setupExperiment(experimentOnRegisteredWithFirstWinning(THE_KEY));

        // this causes the experiment to be persisted
        final UUID uuid = UUID.randomUUID();
        sampleApp.conductExperimentByUser(THE_KEY, "FALLBACK", uuid);

        // flip the toggle so that group 'b' is now the winning group
        petri.updateExperiment(updateExperimentState(experiment, new TestGroup(1, 0, "a"), new TestGroup(2, 100, "b")));

        assertThat(sampleApp.conductExperimentByUser(THE_KEY, "FALLBACK", uuid), is("a"));
    }


    @Test
    public void conductsExperimentAndReportIt() throws IOException, InterruptedException {
        final Experiment experiment = setupExperiment(experimentWithFirstWinning(THE_KEY));
        sampleApp.conductExperiment(THE_KEY, "FALLBACK");
        assertConductExperimentReported(experiment);
    }

    @Test
    public void reportsSingleSpecToPetri() throws Exception {
        String SYNC_SPECS_URL = "http://localhost:" + SAMPLE_APP_PORT + "/sync-specs";

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(SYNC_SPECS_URL);
        HttpResponse response = httpClient.execute(post);
        assertThat(response.getStatusLine().getStatusCode(), CoreMatchers.is(HttpStatus.SC_OK));

        List<ExperimentSpec> specs = petri.fetchSpecs();

        assertThat(specs.size(), CoreMatchers.is(1));
        assertThat(specs.get(0).getKey(), CoreMatchers.is(ValidStubSpecDefinition_1.class.getName()));
        assertThat(specs.get(0).getTestGroups(), CoreMatchers.is(ValidStubSpecDefinition_1.testGroups));
        assertThat(specs.get(0).getScopes(), CoreMatchers.is(Arrays.asList(ValidStubSpecDefinition_1.scopeDefinitions)));
    }

    @Test
    public void doesNotUsePetriServerOnUserRequest() throws Exception {
        setupExperiment(experimentWithFirstWinning(THE_KEY));
        petri.failNextReuqest();
        assertThat(sampleApp.conductExperiment(THE_KEY, "FALLBACK"), is("a"));
    }

}
