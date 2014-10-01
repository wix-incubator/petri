package com.wixpress.common.petri.e2e;

import com.wixpress.petri.Main;
import com.wixpress.petri.PetriConfigFile;
import com.wixpress.petri.PetriRPCClient;
import com.wixpress.common.petri.testutils.ServerRunner;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.test.SampleAppRunner;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import util.DBDriver;


import java.net.MalformedURLException;

import static com.wixpress.petri.PetriConfigFile.aPetriConfigFile;
import static com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.*;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.*;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 8/21/14
 * Time: 12:10 PM
 * To change this template use File | Settings | File Templates.
 */


public class PetriE2eTest {


    private static final int PETRI_PORT = 9010;

    private static final int SAMPLE_APP_PORT = 9011;
    private static final String SAMPLE_WEBAPP_PATH = PetriE2eTest.class.getResource("/").getPath() + "../../../sample-petri-app/src/main/webapp";
    private static final SampleAppRunner sampleAppRunner = new SampleAppRunner(SAMPLE_APP_PORT, SAMPLE_WEBAPP_PATH);

    @BeforeClass
    public static void startServers() throws Exception {

        // TODO: Remove duplication with RPCPetriServerTest
        DBDriver dbDriver = DBDriver.dbDriver("jdbc:h2:mem:test;IGNORECASE=TRUE");
        dbDriver.createSchema();

        aPetriConfigFile().delete();
        aPetriConfigFile().
                withUsername("auser").
                withPassword("sa").
                withUrl("jdbc:h2:mem:test").
                withPort(PETRI_PORT).
                save();

        Main.main();

        sampleAppRunner.start();
    }

    @AfterClass
    public static void stopServers() throws Exception {
        sampleAppRunner.stop();
        aPetriConfigFile().delete();
    }

    private PetriClient petriClient() throws MalformedURLException {
        return PetriRPCClient.makeFor("http://localhost:" +
                PETRI_PORT +
                "/petri/");
    }


    @Test
    public void conductingASimpleExperiment() throws Exception {

        PetriClient petriClient = petriClient();
        petriClient.addSpecs(asList(
                aNewlyGeneratedExperimentSpec("THE_KEY").
                    withTestGroups(asList("a", "b")).
                    withScopes(new ScopeDefinition("the scope", false)).
                build()));

        DateTime now = new DateTime();
        petriClient.insertExperiment(
                anExperimentSnapshot().
                        withStartDate(now.minusMinutes(1)).
                        withEndDate(now.plusYears(1)).
                        withKey("THE_KEY").
                    withGroups(asList(new TestGroup(0, 100, "a"), new TestGroup(1, 0, "b"))).
                    withOnlyForLoggedInUsers(false).
                build());
        assertThat(petriClient.fetchActiveExperiments().size(), is(1));

        String testResult = sampleAppRunner.conductExperiment("THE_KEY","FALLBACK_VALUE");
        assertThat(testResult, is("a"));

    }


}
