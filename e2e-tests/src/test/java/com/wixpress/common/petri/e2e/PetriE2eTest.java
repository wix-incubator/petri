package com.wixpress.common.petri.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.wixpress.petri.PetriRPCClient;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentBuilder;
import com.wixpress.petri.experiments.domain.ExperimentSnapshot;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.UUID;

import static com.wixpress.petri.test.TestBuilders.experimentOnRegisteredWithFirstWinning;
import static com.wixpress.petri.test.TestBuilders.experimentWithFirstWinning;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * Petri - (c) Wix LTD. http://www.wix.com
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

        assertThat(petriClient.fetchActiveExperiments().size(), is(1));

        String testResult = sampleAppRunner.conductExperiment("THE_KEY", "FALLBACK_VALUE");
        assertThat(testResult, is("a"));
    }

    @Test
    public void conductingAnExperimentWithDynamicallyLoadedFiltersAndCustomContext() throws Throwable {
        addSpec("THE_KEY");

        ExperimentSnapshot experiment = experimentWithFirstWinning("THE_KEY").build();
        petriJsonClient().invoke("insertExperiment", new JsonNode[]{experimentWithCustomUserTypeFilter(experiment)});

        String testResult = sampleAppRunner.conductExperimentWithCustomContext("THE_KEY", "FALLBACK_VALUE");
        assertThat(testResult, is("a"));
    }

    @Test
    public void afterPauseUserDoesNotLooseExperienceEvenWhenNoCookie_akaServerSideState() throws IOException {
        addSpec("THE_KEY");
        fullPetriClient.insertExperiment(experimentOnRegisteredWithFirstWinning("THE_KEY").build());

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



}
