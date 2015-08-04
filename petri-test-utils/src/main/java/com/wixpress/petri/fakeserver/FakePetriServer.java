package com.wixpress.petri.fakeserver;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentBuilder;
import com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder;
import com.wixpress.petri.petri.ConductExperimentSummary;
import com.wixpress.petri.petri.RAMPetriClient;
import com.wixpress.petri.petri.SpecDefinition;

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
    private final TestJsonRPCServer petriServer;
    private RAMPetriClient petriClient;

    public FakePetriServer(int port){
        petriClient = new RAMPetriClient();
        petriServer = new TestJsonRPCServer(petriClient, makeObjectMapper(), port);
    }

    public void start() throws Exception {
        petriServer.start();
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

    public void failNextReuqest(){
        petriClient.setBlowUp(true);
    }



}
