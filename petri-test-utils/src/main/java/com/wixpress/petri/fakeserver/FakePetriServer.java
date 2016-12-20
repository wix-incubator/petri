package com.wixpress.petri.fakeserver;

import com.wix.hoopoe.koboshi.it.RemoteDataFetcherDriver;
import com.wixpress.petri.experiments.domain.*;
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
    private RemoteDataFetcherDriver remoteDataFetcherDriver;

    public FakePetriServer(int port, int portOfSUT) {
        petriClient = new RAMPetriClient();
        petriServer = new TestJsonRPCServer(petriClient, makeObjectMapper(), port);
        remoteDataFetcherDriver = new RemoteDataFetcherDriver("localhost", portOfSUT);
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
        Experiment createdExperiment = petriClient.insertExperiment(experiment.build());
        updateTheAppsCacheNow();
        return createdExperiment;
    }

    public void updateExperiment(ExperimentBuilder experimentBuilder) {
        petriClient.updateExperiment(experimentBuilder.build());
        updateTheAppsCacheNow();
    }

    public List<ConductExperimentSummary> getConductExperimentReport(int experimentId) {
        return petriClient.getExperimentReport(experimentId);
    }

    public void failNextRequest() {
        petriClient.setBlowUp(true);
    }

    public List<ExperimentSpec> fetchSpecs() {
        return petriClient.fetchSpecs();
    }

    public void updateTheAppsCacheNow() {
        remoteDataFetcherDriver.fetch(ConductibleExperiments.class);
    }
}
