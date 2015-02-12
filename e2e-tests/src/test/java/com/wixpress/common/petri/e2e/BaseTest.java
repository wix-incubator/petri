package com.wixpress.common.petri.e2e;

import com.wixpress.petri.Main;
import com.wixpress.petri.PetriRPCClient;
import com.wixpress.petri.experiments.domain.ExperimentSnapshot;
import com.wixpress.petri.experiments.domain.TestGroup;
import com.wixpress.petri.laboratory.http.LaboratoryFilter;
import com.wixpress.petri.petri.FullPetriClient;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.test.SampleAppRunner;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import util.DBDriver;

import java.net.MalformedURLException;

import static com.wixpress.petri.PetriConfigFile.aPetriConfigFile;
import static com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.anExperimentSnapshot;
import static com.wixpress.petri.experiments.domain.ScopeDefinition.aScopeDefinitionForAllUserTypes;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.aNewlyGeneratedExperimentSpec;
import static java.util.Arrays.asList;

/**
 * User: Dalias
 * Date: 2/8/15
 * Time: 8:57 AM
 */
public abstract class BaseTest {

    protected static final int PETRI_PORT = 9010;

    protected static final int SAMPLE_APP_PORT = 9011;
    protected static final String SAMPLE_WEBAPP_PATH = PetriReportsTest.class.getResource("/").getPath() + "../../../sample-petri-app/src/main/webapp";
    protected static SampleAppRunner sampleAppRunner ;
    protected static DBDriver dbDriver;
    protected FullPetriClient fullPetriClient;
    protected PetriClient petriClient;

    protected void addSpec(String key) {
        petriClient.addSpecs(asList(
                aNewlyGeneratedExperimentSpec(key).
                        withTestGroups(asList("a", "b")).
                        withScopes(aScopeDefinitionForAllUserTypes("the scope")).
                        build()));
    }

    protected ExperimentSnapshot experimentWithFirstWinning(String key) {
        DateTime now = new DateTime();
        return anExperimentSnapshot().
                withStartDate(now.minusMinutes(1)).
                withEndDate(now.plusYears(1)).
                withKey(key).
                withGroups(asList(new TestGroup(0, 100, "a"), new TestGroup(1, 0, "b"))).
                withOnlyForLoggedInUsers(false).
                build();
    }

    @BeforeClass
    public static void startServers() throws Exception {

        sampleAppRunner = new SampleAppRunner(SAMPLE_APP_PORT, SAMPLE_WEBAPP_PATH, 1);

        // TODO: Remove duplication with RPCPetriServerTest
        dbDriver = DBDriver.dbDriver("jdbc:h2:mem:test;IGNORECASE=TRUE");
        dbDriver.createSchema();
        dbDriver.createMetricsTableSchema();

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

    @Before
    public void start() throws MalformedURLException {
        petriClient = petriClient();
        fullPetriClient = fullPetriClient();
    }


    @AfterClass
    public static void stopServers() throws Exception {
        sampleAppRunner.stop();
        aPetriConfigFile().delete();
        dbDriver.closeConnection();
    }

    protected FullPetriClient fullPetriClient() throws MalformedURLException {
        return PetriRPCClient.makeFullClientFor("http://localhost:" +
                PETRI_PORT +
                "/petri/full_api");
    }

    protected PetriClient petriClient() throws MalformedURLException {
        return PetriRPCClient.makeFor("http://localhost:" +
                PETRI_PORT +
                "/petri/api");
    }

}
