package com.wixpress.petri.test;

import com.wixpress.petri.PetriRPCClient;
import com.wixpress.petri.JsonRPCServer;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentBuilder;
import com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder;
import com.wixpress.petri.petri.*;

import java.util.List;

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
    private FullPetriClient petriClient;

    public FakePetriServer(int port){
        petriServer = new JsonRPCServer(new RAMPetriClient(), makeObjectMapper(), port);
        this.port = port;
    }

    public void start() throws Exception {
        petriServer.start();
        petriClient = PetriRPCClient.makeFullClientFor("http://localhost:" +
                port +
                "/petri");
    }

    public void stop() throws Exception {
        petriServer.stop();
    }

    public void addSpec(SpecDefinition.ExperimentSpecBuilder spec) {
        petriClient.addSpecs(asList(spec.build()));
    }

    public Experiment addExperiment(ExperimentSnapshotBuilder experiment) {
        return petriClient.insertExperiment(experiment.build());
    }

    public void updateExperiment(ExperimentBuilder experimentBuilder) {
        petriClient.updateExperiment(experimentBuilder.build());
    }

    public List<ConductExperimentSummary> getConductExperimentReport(int experimentId) {
        return petriClient.getExperimentReport(experimentId);
    }
}
