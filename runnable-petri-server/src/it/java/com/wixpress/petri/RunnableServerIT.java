package com.wixpress.petri;


import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.wixpress.petri.petri.FullPetriClient;
import com.wixpress.petri.test.TestBuilders;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.ConnectException;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;


public class RunnableServerIT {

    private static final int SERVER_STARTUP_RETRIES = 60;
    private static final String BASE_SERVER_ADDRESS = "http://localhost:9011/";
    private static final String BASE_PETRI_API_ADDRESS = BASE_SERVER_ADDRESS + "petri";

    private FullPetriClient fullPetriClient;


    private Process petriServerProcess;

    @Before
    public void setUp() throws Exception {
        removeDBFile();
        fullPetriClient = PetriRPCClient.makeFullClientFor(BASE_PETRI_API_ADDRESS);
        runServer();
    }


    @After
    public void tearDown() throws Exception {
        stopServer();
    }

    @Test
    public void testServerInit() throws Exception {
        assertThat(fullPetriClient.fetchAllExperiments(), hasSize(0));
    }

    @Test
    public void testServerPersistence() throws Exception {
        fullPetriClient.addSpecs(Collections.singletonList(TestBuilders.abSpecBuilder("someSpec").build()));
        assertThat(fullPetriClient.fetchSpecs(), hasSize(1));
        restartServer();
        assertThat(fullPetriClient.fetchSpecs(), hasSize(1));
    }

    public static void removeDBFile() throws Exception {
        FileUtils.deleteQuietly(new File("petri.db.trace.db"));
        FileUtils.deleteQuietly(new File("petri.db.mv.db"));
    }

    private void runServer() throws Exception {
        runPetriServer();
        waitForServer(ServerState.STARTED);
    }

    private void stopServer() throws Exception {
        stopPetriServer();
        waitForServer(ServerState.STOPPED);
    }

    private void restartServer() throws Exception {
        stopServer();
        runServer();
    }

    private void runPetriServer() throws Exception {
        String[] command = "java -jar target/runnable-petri-server-1.19.0-SNAPSHOT.jar".split(" ");
        petriServerProcess = new ProcessBuilder(command).start();
    }

    private void stopPetriServer() throws Exception {
        petriServerProcess.destroy();
        petriServerProcess = null;
    }

    private void waitForServer(ServerState expected) throws Exception {
        final BoundRequestBuilder request = new AsyncHttpClient().prepareGet(BASE_SERVER_ADDRESS);
        for (int i = 0; i < SERVER_STARTUP_RETRIES; i++) {
            ServerState res;
            try {
                request.execute().get();
                res = ServerState.STARTED;
            } catch (Exception e) {
                if (e.getCause() instanceof ConnectException) res = ServerState.STOPPED;
                else throw new Exception("waiting for server got unexpected error: " + e.getMessage(), e);
            }
            if (res == expected) return;
            Thread.sleep(1000);
        }
        throw new Exception("Server did not change to " + expected + " state");
    }


}


enum ServerState {STARTED, STOPPED}