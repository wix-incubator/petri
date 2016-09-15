package com.wixpress.guineapig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.natpryce.makeiteasy.Maker;
import com.wixpress.guineapig.drivers.HttpDriver;
import com.wixpress.guineapig.drivers.JsonResponse;
import com.wixpress.guineapig.entities.ui.UiExperiment;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.petri.RAMPetriClient;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static com.wixpress.guineapig.TestUtils.extractCollectionPayload;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.aNewlyGeneratedExperimentSpec;

/**
 * Created with IntelliJ IDEA.
 * User: igalharel
 * Date: 9/23/13
 * Time: 1:07 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(JUnit4.class)
abstract public class BrokenITBase {

    protected static final String BASE_URL = "http://127.0.0.1:9901/";
    protected static final String BASE_API_URL = BASE_URL + "api/v1/";

    @Autowired
    protected RAMPetriClient ramPetriClient;

    @Before
    public void setup() {
        //ramPetriClient.clearAll();
    }

    protected void givenPetriContains(Maker<Experiment> experiment) {
        givenPetriContains(experiment.make());
    }

    protected void givenPetriContains(Experiment experiment) {
        ramPetriClient.addSpecs(ImmutableList.of(aNewlyGeneratedExperimentSpec(experiment.getKey()).build()));
        ramPetriClient.insertExperiment(experiment.getExperimentSnapshot());
    }

    public HttpDriver httpDriver = new HttpDriver();

    public ObjectMapper om = ObjectMapperFactory.makeObjectMapper();

    protected List<UiExperiment> getExperiments() throws IOException {
        JsonResponse response = httpDriver.get(BASE_API_URL + "Experiments");

        return extractCollectionPayload(response, new TypeReference<List<UiExperiment>>() {
        });
    }
}
