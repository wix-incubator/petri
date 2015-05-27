package com.wixpress.petri;

import com.wixpress.petri.petri.FullPetriClient;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.petri.PetriClientContractTest;
import com.wixpress.petri.petri.UserRequestPetriClient;
import org.junit.*;
import util.DBDriver;

import java.net.MalformedURLException;
import java.sql.SQLException;

import static com.wixpress.petri.PetriConfigFile.aPetriConfigFile;

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
}
