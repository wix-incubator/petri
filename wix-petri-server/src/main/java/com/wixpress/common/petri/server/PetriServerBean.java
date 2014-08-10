package com.wixpress.common.petri.server;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSnapshot;
import com.wixpress.petri.experiments.domain.ExperimentSpec;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.petri.RAMPetriClient;
import org.springframework.stereotype.Service;

import java.util.List;

/** PetriClient Web Service implementation
 * Created by avrahamr on 8/7/14.
 */
@Service("petriServerBean")
public class PetriServerBean implements PetriClient {

    private PetriClient ramPetri = new RAMPetriClient();


    @Override
    public List<Experiment> fetchActiveExperiments() {
        return ramPetri.fetchActiveExperiments();
    }

    @Override
    public List<Experiment> fetchAllExperiments() {
        return ramPetri.fetchAllExperiments();
    }

    @Override
    public List<Experiment> fetchAllExperimentsGroupedByOriginalId() {
        return ramPetri.fetchAllExperimentsGroupedByOriginalId();
    }

    @Override
    public Experiment insertExperiment(ExperimentSnapshot snapshot) {
        return ramPetri.insertExperiment(snapshot);
    }

    @Override
    public Experiment updateExperiment(Experiment experiment) {
        return ramPetri.updateExperiment(experiment);
    }

    @Override
    public List<ExperimentSpec> fetchSpecs() {
        return ramPetri.fetchSpecs();
    }

    @Override
    public void addSpecs(List<ExperimentSpec> expectedSpecs) {
        ramPetri.addSpecs(expectedSpecs);
    }

    @Override
    public List<Experiment> getHistoryById(int id) {
        return ramPetri.getHistoryById(id);
    }

    @Override
    public void deleteSpec(String key) {
        ramPetri.deleteSpec(key);
    }
}
