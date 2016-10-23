package com.wixpress.common.petri.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.wixpress.petri.PetriRPCClient;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.UUID;

import static com.wixpress.petri.test.TestBuilders.experimentOnRegisteredWithFirstWinning;
import static com.wixpress.petri.test.TestBuilders.experimentWithFirstWinning;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotEquals;
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
        return PetriRPCClient.getJsonRpcHttpClient(petriServiceUrl + "/full_api");
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
        fullPetriClient.insertExperiment(experimentWithFirstWinning("THE_KEY").build());
        sampleAppRunner.updateTheCacheNow();

        assertThat(petriClient.fetchActiveExperiments().size(), is(1));

        String testResult = sampleAppRunner.conductExperiment("THE_KEY", "FALLBACK_VALUE");
        assertThat(testResult, is("a"));
    }

    @Test
    public void conductingAnExperimentWithDynamicallyLoadedFiltersAndCustomContext() throws Throwable {
        addSpec("THE_KEY");

        ExperimentSnapshot experiment = experimentWithFirstWinning("THE_KEY").build();
        petriJsonClient().invoke("insertExperiment", new JsonNode[]{experimentWithCustomUserTypeFilter(experiment)});
        sampleAppRunner.updateTheCacheNow();

        String testResult = sampleAppRunner.conductExperimentWithCustomContext("THE_KEY", "FALLBACK_VALUE");
        assertThat(testResult, is("a"));
    }

    @Test
    public void afterPauseUserDoesNotLooseExperienceEvenWhenNoCookie_akaServerSideState() throws IOException {
        addSpec("THE_KEY");
        fullPetriClient.insertExperiment(experimentOnRegisteredWithFirstWinning("THE_KEY").build());
        sampleAppRunner.updateTheCacheNow();


        UUID uuid = UUID.randomUUID();

        String testResult = sampleAppRunner.conductExperimentByUser("THE_KEY", "FALLBACK_VALUE", uuid);
        assertThat(testResult, is("a"));

        Experiment experiment = petriClient.fetchActiveExperiments().get(0);
        fullPetriClient.updateExperiment(
                ExperimentBuilder.anExperiment().withId(1).withLastUpdated(experiment.getLastUpdated()).
                        withExperimentSnapshot(experimentOnRegisteredWithFirstWinning("THE_KEY").withPaused(true).build())
                        .build());

        testResult = sampleAppRunner.conductExperimentByUserWithNoPreviousCookies("THE_KEY", "FALLBACK_VALUE", uuid);
        assertThat(testResult, is("a"));
    }

    @Test
    public void validateSpecSyncAvailability() throws Exception {
        String syncSpecsUrl = "http://localhost:" + SAMPLE_APP_PORT + "/sync-specs";

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(syncSpecsUrl);
        HttpResponse response = httpClient.execute(post);
        assertThat(response.getStatusLine().getStatusCode(), CoreMatchers.is(HttpStatus.SC_OK));

        List<ExperimentSpec> specs = fullPetriClient.fetchSpecs();

        assertNotEquals(specs.size(), 0);
    }
}
