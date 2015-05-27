package com.wixpress.petri;

import com.google.common.collect.ImmutableList;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder;
import com.wixpress.petri.petri.*;
import org.junit.*;
import util.DBDriver;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.List;

import static com.wixpress.petri.PetriConfigFile.aPetriConfigFile;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 9/7/14
 * Time: 12:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class RPCPetriServerTest extends PetriClientContractTest {

    private final FullPetriClient fullPetriClient;
    private final PetriClient petriClient;
    private final UserRequestPetriClient userRequestPetriClient;
    private static DBDriver dbDriver;

    @BeforeClass
    public static void startPetriServer() throws Exception {
        dbDriver = DBDriver.dbDriver("jdbc:h2:mem:test;IGNORECASE=TRUE");
        dbDriver.createSchema();
        aPetriConfigFile().delete();
        aPetriConfigFile().
                withUsername("auser").
                withPassword("sa").
                withUrl("jdbc:h2:mem:test").
                withPort(9011).
                save();

        Main.main();
    }

    @AfterClass
    public static void afterClass() throws SQLException {
        aPetriConfigFile().delete();
        dbDriver.closeConnection();

    }

    public RPCPetriServerTest() throws Exception{
        fullPetriClient = PetriRPCClient.makeFullClientFor("http://localhost:9011/petri/full_api");
        petriClient = PetriRPCClient.makeFor("http://localhost:9011/petri/api");
        userRequestPetriClient = PetriRPCClient.makeUserRequestFor("http://localhost:9011/petri/user_request_api");
    }

    @Before
    public void clearDBSchema () throws SQLException, ClassNotFoundException {
        dbDriver.createSchema();
    }

    @Override
    protected FullPetriClient fullPetriClient() {
        return fullPetriClient;
    }

    @Override
    protected PetriClient petriClient() {
        return petriClient;
    }

    @Override
    protected UserRequestPetriClient synchPetriClient() {
        return userRequestPetriClient;
    }

    @Test(expected = Exception.class)
    public void respondsWithErrorForIrrelevantURLs() throws MalformedURLException {
        PetriRPCClient.makeFullClientFor("http://localhost:9011/SOME_OTHER_SERVICE").fetchSpecs();
    }

    @Test(expected = NonSerializableServerException.class)
    public void throwsSpecialExceptionIfServerExceptionIsNotSerializable() {
        dbDriver.dropTables();
        // should cause an exception on the server that is not serializable
        petriClient.fetchActiveExperiments();
    }

    @Test
    public void pauseExperimentWhenReachedConductionLimit() throws InterruptedException {
        ExperimentSnapshotBuilder experimentWithConductionLimit = anActiveSnapshot.withConductLimit(2);
        Experiment experiment = addExperimentWithKey(experimentWithConductionLimit);

        List<ConductExperimentReport> conductedExperiments = ImmutableList.of(new ConductExperimentReport("localhost", experiment.getId(), "true", 3l));
        petriClient().reportConductExperiment(conductedExperiments);

        int attempts = 0;
        while(!petriClient().fetchActiveExperiments().get(0).isPaused() && attempts < 10){
            Thread.sleep(5000l); attempts++;
        }

        assertTrue(petriClient().fetchActiveExperiments().get(0).isPaused());

        //TODO - once PetriNotifier is implemented properly by sending emails or something, add test for the title and recipients
        //String expectedTitle = "Experiment "+ experiment.getName() + " id:" + experiment.getId() + " paused due to conduction limit reach";
        //assert something on recipients
    }
}
