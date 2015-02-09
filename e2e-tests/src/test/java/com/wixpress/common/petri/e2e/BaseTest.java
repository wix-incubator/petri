package com.wixpress.common.petri.e2e;

import com.wixpress.petri.Main;
import com.wixpress.petri.PetriRPCClient;
import com.wixpress.petri.petri.FullPetriClient;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.test.SampleAppRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import util.DBDriver;

import java.net.MalformedURLException;

import static com.wixpress.petri.PetriConfigFile.aPetriConfigFile;

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

    @BeforeClass
    public static void startServers() throws Exception {

        sampleAppRunner = new SampleAppRunner(SAMPLE_APP_PORT, SAMPLE_WEBAPP_PATH);

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
