package com.wixpress.common.petri.e2e;

import com.wixpress.petri.Main;
import com.wixpress.petri.PetriRPCClient;
import com.wixpress.petri.petri.DBDriver;
import com.wixpress.petri.petri.FullPetriClient;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.test.SampleAppRunner;
import com.wixpress.petri.test.TestBuilders;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.net.MalformedURLException;

import static com.wixpress.petri.PetriConfigFile.aPetriConfigFile;
import static java.util.Arrays.asList;

/**
 * User: Dalias
 * Date: 2/8/15
 * Time: 8:57 AM
 */
public abstract class BaseTest {

    protected static final int PETRI_PORT = 9010;
    protected static final String petriServiceUrl = "http://localhost:" + PETRI_PORT + "/petri";

    protected static final int SAMPLE_APP_PORT = 9011;
    protected static final String SAMPLE_WEBAPP_PATH = PetriReportsTest.class.getResource("/").getPath() + "../../../sample-petri-app/src/main/webapp";
    protected static SampleAppRunner sampleAppRunner ;
    protected static DBDriver dbDriver;

    protected FullPetriClient fullPetriClient;
    protected PetriClient petriClient;


    protected void addSpec(String key) {
        petriClient.addSpecs(asList(TestBuilders.abSpecBuilder(key).build()));
    }

    @BeforeClass
    public static void startServers() throws Exception {

        sampleAppRunner = new SampleAppRunner(SAMPLE_APP_PORT, SAMPLE_WEBAPP_PATH, 1, true);

        // TODO: Remove duplication with RPCPetriServerTest
        dbDriver = DBDriver.dbDriver("jdbc:h2:mem:test;IGNORECASE=TRUE");
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

    @Before
    public void start() throws MalformedURLException {
        petriClient = petriClient();
        fullPetriClient = fullPetriClient();

        dbDriver.emptyTables();
    }


    @AfterClass
    public static void stopServers() throws Exception {
        sampleAppRunner.stop();
        aPetriConfigFile().delete();
        dbDriver.closeConnection();
    }

    protected FullPetriClient fullPetriClient() throws MalformedURLException {
        return PetriRPCClient.makeFullClientFor(petriServiceUrl);
    }

    protected PetriClient petriClient() throws MalformedURLException {
        return PetriRPCClient.makeFor(petriServiceUrl);
    }

}
