package com.wixpress.petri.test;

import com.wixpress.petri.PetriRPCClient;
import com.wixpress.petri.JsonRPCServer;
import com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.petri.RAMPetriClient;
import com.wixpress.petri.petri.SpecDefinition;

import static com.wixpress.petri.experiments.jackson.ObjectMapperFactory.makeObjectMapper;
import static java.util.Arrays.asList;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 9/2/14
* Time: 11:54 AM
* To change this template use File | Settings | File Templates.
*/
public class FakePetriServer {
    private final JsonRPCServer petriServer;
    private final int port;
    private PetriClient petriClient;

    public FakePetriServer(int port){
        petriServer = new JsonRPCServer(new RAMPetriClient(), makeObjectMapper(), port, PetriClient.class);
        this.port = port;
    }

    public void start() throws Exception {
        petriServer.start();
        petriClient = PetriRPCClient.makeFor("http://localhost:" +
                port +
                "/petri/");
    }

    public void stop() throws Exception {
        petriServer.stop();
    }

    public void addSpec(SpecDefinition.ExperimentSpecBuilder spec) {
        petriClient.addSpecs(asList(spec.build()));
    }

    public void addExperiment(ExperimentSnapshotBuilder experiment) {
        petriClient.insertExperiment(experiment.build());
    }
}
