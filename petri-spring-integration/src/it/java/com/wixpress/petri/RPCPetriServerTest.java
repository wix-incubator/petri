package com.wixpress.petri;

import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.petri.PetriClientContractTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import util.DBDriver;

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

    private final PetriClient petriClient;
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
    public static void afterClass() {
        aPetriConfigFile().delete();
    }

    public RPCPetriServerTest() throws Exception{
        petriClient = PetriRPCClient.makeFor("http://localhost:9011/petri");

    }

    @Before
    public void clearDBSchema () throws SQLException, ClassNotFoundException {
        dbDriver.createSchema();
    }

    @Override
    protected PetriClient petriClient() {
        return petriClient;
    }
}
