package com.wixpress.common.petri.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.wixpress.petri.PetriRPCClient;
import com.wixpress.petri.experiments.domain.ExperimentSnapshot;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import org.junit.Test;

import java.net.MalformedURLException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 8/21/14
 * Time: 12:10 PM
 * To change this template use File | Settings | File Templates.
 */


public class PetriE2eTest extends BaseTest {


    private static JsonRpcHttpClient petriJsonClient() throws MalformedURLException {
        return PetriRPCClient.getJsonRpcHttpClient("http://localhost:" +
                PETRI_PORT +
                "/petri/full_api");
    }

    // 'UserType' filter is not on the classpath, therefore added manually as text
    // ( see in the e2e-tests pom.xml how the sample-extended-filters jar is copied via maven-antrun-plugin)
    private JsonNode experimentWithCustomUserTypeFilter(ExperimentSnapshot experiment) {
        JsonNode jsonNode = ObjectMapperFactory.makeObjectMapper().valueToTree(experiment);
        ((ObjectNode)jsonNode).putArray("filters").addObject().put("filter-type", "UserType");
        return jsonNode;
    }


    @Test
    public void conductingASimpleExperiment() throws Exception {
        addSpec("THE_KEY");
        fullPetriClient.insertExperiment(experimentWithFirstWinning("THE_KEY"));

        assertThat(petriClient.fetchActiveExperiments().size(), is(1));

        String testResult = sampleAppRunner.conductExperiment("THE_KEY", "FALLBACK_VALUE");
        assertThat(testResult, is("a"));

    }

    @Test
    public void conductingAnExperimentWithDynamicallyLoadedFiltersAndCustomContext() throws Throwable {
        addSpec("THE_KEY");

        ExperimentSnapshot experiment = experimentWithFirstWinning("THE_KEY");
        petriJsonClient().invoke("insertExperiment", new JsonNode[]{experimentWithCustomUserTypeFilter(experiment)});

        String testResult = sampleAppRunner.conductExperimentWithCustomContext("THE_KEY", "FALLBACK_VALUE");
        assertThat(testResult, is("a"));
    }


}
