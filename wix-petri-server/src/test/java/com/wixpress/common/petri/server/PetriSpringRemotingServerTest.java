package com.wixpress.common.petri.server;


import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentBuilder;
import com.wixpress.petri.experiments.domain.ExperimentSpec;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.petri.PetriClient;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class PetriSpringRemotingServerTest {

    public static final int PORT = 9924;
    public static final String CONTEXT_PATH = "/";

    private static JettyLocalServletServer jettyLocal;

    private PetriClient petriClient;

    public static class PetriServerProxy {
        public static PetriClient makeFor(String serviceUrl) throws MalformedURLException {

            JsonRpcHttpClient client = new JsonRpcHttpClient(ObjectMapperFactory.makeObjectMapper(),
                    new URL(serviceUrl),
                    new HashMap<String, String>());


            return ProxyUtil.createClientProxy(
                    PetriServerProxy.class.getClassLoader(),
                    PetriClient.class,
                    client);

        }
    }


    @BeforeClass
    public static void setUpClass() throws Exception {
        jettyLocal = new JettyLocalServletServer();
        final String baseDir = PetriSpringRemotingServerTest.class.getResource("/").getPath();
        jettyLocal.startServer(PORT, CONTEXT_PATH, baseDir + "../../src/main/webapp");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        jettyLocal.stopServer();
    }

    @Before
    public void setup() throws MalformedURLException {
        petriClient = PetriServerProxy.makeFor("http://localhost:" +
                PORT +
                "/wix/petri");
    }


    @Test
    public void testFetchActiveExperiments() throws Exception {
        List<Experiment> resp = petriClient.fetchActiveExperiments();
        Assert.notNull(resp);
    }

    @Test
    public void testFetchAllExperiments() throws Exception {
        List<Experiment> resp = petriClient.fetchAllExperiments();
        Assert.notNull(resp);
    }

    @Test
    public void testFetchAllExperimentsGroupedByOriginalId() throws Exception {
        List<Experiment> resp = petriClient.fetchAllExperimentsGroupedByOriginalId();
        Assert.notNull(resp);
    }

    @Test
    public void testDeleteSpec() throws Exception {
        petriClient.deleteSpec("toDelete");
    }

    @Test
    public void testFetchSpecs() throws Exception {
        List<ExperimentSpec> resp = petriClient.fetchSpecs();
        Assert.notNull(resp);
    }
}