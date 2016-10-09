package com.wixpress.petri;

import com.google.common.collect.ImmutableList;
import com.wixpress.guineapig.drivers.HttpDriver;
import com.wixpress.guineapig.drivers.JsonResponse;
import com.wixpress.guineapig.entities.ui.UiExperiment;
import com.wixpress.guineapig.entities.ui.UiExperimentBuilder;
import com.wixpress.guineapig.entities.ui.UiTestGroup;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder;
import com.wixpress.petri.petri.*;
import org.hamcrest.CoreMatchers;
import org.joda.time.DateTime;
import org.junit.*;

import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static com.wixpress.petri.PetriConfigFile.aPetriConfigFile;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
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
    private final HttpDriver httpDriver;
    private static final String BASE_SERVER_UI_ADDRESS = "http://localhost:9011";

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
                withAddBackOfficeWebapp(true).
                save();

        Main.main();
    }

    @AfterClass
    public static void afterClass() throws SQLException {
        aPetriConfigFile().delete();
        dbDriver.closeConnection();

    }

    public RPCPetriServerTest() throws Exception {
        String serviceUrl = BASE_SERVER_UI_ADDRESS + "/petri";
        fullPetriClient = PetriRPCClient.makeFullClientFor(serviceUrl);
        petriClient = PetriRPCClient.makeFor(serviceUrl);
        userRequestPetriClient = PetriRPCClient.makeUserRequestFor(serviceUrl);
        httpDriver = new HttpDriver();
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

    @Test
    public void testUiEndpoints() throws Exception {
        //OR!!! - if this can be selenium then i am literally DONE :)
        String someExpKey = "someExpKey";
        createExperimentViaUiEndpoint(someExpKey);

        experimentExistsInPetriServer(someExpKey);
        experimentAccessibleFromUi(someExpKey);
    }

    private void experimentAccessibleFromUi(String someExpKey) {
        JsonResponse jsonResponse = httpDriver.get(BASE_SERVER_UI_ADDRESS + "/v1/Experiment/1");
        assertThat(jsonResponse.getBodyRaw(), containsString(someExpKey));
    }

    private void experimentExistsInPetriServer(String someExpKey) {
        List<Experiment> experiments = fullPetriClient.fetchAllExperiments();
        assertThat(experiments, hasSize(1));
        assertThat(experiments.get(0).getExperimentSnapshot().key(), CoreMatchers.is(someExpKey));
    }

    private void createExperimentViaUiEndpoint(String someExperimentName){
        UiExperiment uiExperiment = UiExperimentBuilder
                .anUiExperiment()
                .withKey(someExperimentName)
                .withSpecKey(false)
                .withScope("publicUrl")
                .withGroups(Arrays.asList(new UiTestGroup(1, "old", 0), new UiTestGroup(2, "new", 100)))
                .withEndDate(new DateTime().plusYears(1).getMillis())
                .build();

        httpDriver.post(BASE_SERVER_UI_ADDRESS + "/v1/Experiments", uiExperiment);
    }
}
