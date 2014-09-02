package com.wixpress.common.petri.e2e;

import com.wixpress.common.petri.PetriServerProxy;
import com.wixpress.common.petri.testutils.ServerRunner;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.test.SampleAppRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


import java.net.MalformedURLException;

import static com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.*;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.*;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 8/21/14
 * Time: 12:10 PM
 * To change this template use File | Settings | File Templates.
 */

@Ignore
public class PetriE2eTest {


    private static final int PETRI_PORT = 9010;
    private static final String PETRI_WEBAPP_PATH = PetriE2eTest.class.getResource("/").getPath() + "../../../wix-petri-server/src/main/webapp";
    private static final  ServerRunner petriRunner = new ServerRunner(PETRI_PORT, PETRI_WEBAPP_PATH);

    private static final int SAMPLE_APP_PORT = 9011;
    private static final String SAMPLE_WEBAPP_PATH = PetriE2eTest.class.getResource("/").getPath() + "../../../sample-petri-app/src/main/webapp";
    private static final SampleAppRunner sampleAppRunner = new SampleAppRunner(SAMPLE_APP_PORT, SAMPLE_WEBAPP_PATH);

    @BeforeClass
    public static void startServers() throws Exception {
        petriRunner.start();
        sampleAppRunner.start();
    }

    @AfterClass
    public static void stopServers() throws Exception {
        sampleAppRunner.stop();
        petriRunner.stop();
    }

    private PetriClient petriClient() throws MalformedURLException {
        return PetriServerProxy.makeFor("http://localhost:" +
                PETRI_PORT +
                "/wix/petri");
    }


    @Test
    public void conductingASimpleExperiment() throws Exception {

        PetriClient petriClient = petriClient();
        petriClient.addSpecs(asList(
                aNewlyGeneratedExperimentSpec("THE_KEY").
                    withTestGroups(asList("a", "b")).
                    withScopes(new ScopeDefinition("the scope", false)).
                build()));

        petriClient.insertExperiment(
                anExperimentSnapshot().
                    withKey("THE_KEY").
                    withGroups(asList(new TestGroup(0, 100, "a"), new TestGroup(1, 0, "b"))).
                    withOnlyForLoggedInUsers(false).
                build());

        String testResult = sampleAppRunner.conductExperiment("THE_KEY","FALLBACK_VALUE");
        assertThat(testResult, is("a"));

        // TODO: replace RamPetriClient with real server
    }


}
