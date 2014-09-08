package com.wixpress.petri;

import com.wixpress.common.petri.PetriRPCClient;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.petri.PetriClientContractTest;
import com.wixpress.petri.test.EmbeddedMysqlDatabase;
import com.wixpress.petri.test.EmbeddedMysqlDatabaseBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import util.DBDriver;

import java.sql.SQLException;

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
    public static void startPetriServer() throws SQLException, ClassNotFoundException {
        dbDriver = DBDriver.dbDriver("jdbc:h2:mem:test;IGNORECASE=TRUE");
        dbDriver.createSchema();
        Main.main();
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
