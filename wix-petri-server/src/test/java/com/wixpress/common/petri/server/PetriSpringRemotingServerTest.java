package com.wixpress.common.petri.server;


import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentBuilder;
import com.wixpress.petri.experiments.domain.ExperimentSpec;
import com.wixpress.petri.petri.PetriClient;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/META-INF/spring/serverApplicationContext-test.xml"

})
public class PetriSpringRemotingServerTest {

    public static final int PORT = 9924;
    public static final String CONTEXT_PATH = "/test";

    static JettyLocalServletServer jettyLocal;

    @Autowired
    @Qualifier ("petriServerProxy")
    PetriClient petriClient;


    @BeforeClass
    public static void setUpClass() throws Exception {


        jettyLocal = new JettyLocalServletServer();

        jettyLocal.startServer(PORT, CONTEXT_PATH, "src/main/webapp");

    }

    @AfterClass
    public static void tearDown() throws Exception {
        jettyLocal.stopServer();
        //      jettyLocalServletServerOrderPage.stopServer();
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
    @Ignore
    public void testGetHistoryById() throws Exception {
        List<Experiment> resp = petriClient.getHistoryById(0);
        Assert.notNull(resp);
    }

    @Test
    @Ignore
    public void testAddSpecs() throws Exception {
        ExperimentSpec spec = createSpec();
        petriClient.addSpecs(Collections.singletonList(spec));
    }

    private ExperimentSpec createSpec() {
        ExperimentSpec spec = new ExperimentSpec("key", "owner", Collections.singletonList("testGroup"), new DateTime(), null, null, false);
        return spec;
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

    @Test
    @Ignore
    public void testInsertExperiment() throws Exception {
        Experiment exp = createExperiment();
        Experiment resp = petriClient.insertExperiment(exp.getExperimentSnapshot());
        Assert.notNull(resp);
    }

    private Experiment createExperiment() {
        ExperimentBuilder builder = new ExperimentBuilder();
        return builder.build();
    }

    @Test
    @Ignore
    public void testUpdateExperiment() throws Exception {
        Experiment exp = createExperiment();
        Experiment resp = petriClient.updateExperiment(exp);
        Assert.notNull(resp);
    }



}