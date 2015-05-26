package com.wixpress.petri.petri;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSpec;

import java.util.List;
import java.util.UUID;

/**
 * @author: talyag
 * @since: 9/10/13
 */
public interface PetriClient {

    //TODO - should change return type : (Experiment for laboratory can be much thinner than for GP)
    //(and rename to fetchConductableExperiments)
    List<Experiment> fetchActiveExperiments();

    void addSpecs(List<ExperimentSpec> expectedSpecs);

    void reportConductExperiment( List<ConductExperimentReport> conductExperimentReports);

    void saveUserState(UUID userId, String userState);

}
