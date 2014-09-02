package com.wixpress.common.petri.e2e;

import com.wixpress.common.petri.PetriServerProxy;
import com.wixpress.common.petri.testutils.ServerRunner;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.petri.PetriClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;


import java.net.URI;

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


    private final static ServerRunner petriDriver = new ServerRunner(9010, petriServerResourceBase());
    private final static ServerRunner sampleAppDriver = new ServerRunner(9011, sampleAppResourceBase());

    @BeforeClass
    public static void startServers() throws Exception {
        petriDriver.start();
        sampleAppDriver.start();
    }

    @AfterClass
    public static void stopServers() throws Exception {
        sampleAppDriver.stop();
        petriDriver.stop();
    }

    @Test
    public void conductingASimpleExperiment() throws Exception {

        PetriClient petriClient = PetriServerProxy.makeFor("http://localhost:9010/wix/petri");
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

        RestTemplate rt = new RestTemplate();
        String testResult = rt.getForObject(new URI("http://localhost:9011/conductExperiment?key=THE_KEY&fallback=FALLBACK"),String.class);
        assertThat(testResult, is("a"));

        // TODO: replace RamPetriClient with real server
    }


    private static String petriServerResourceBase() {
        return PetriE2eTest.class.getResource("/").getPath() + "../../../wix-petri-server/src/main/webapp";
    }

    private static String sampleAppResourceBase() {
        return PetriE2eTest.class.getResource("/").getPath() + "../../../sample-petri-app/src/main/webapp";
    }

}
