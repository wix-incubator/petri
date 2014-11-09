package com.wixpress.petri.petri;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSpec;

import java.util.List;

/**
 * @author: talyag
 * @since: 9/10/13
 */
public interface PetriClient {

    List<Experiment> fetchActiveExperiments();

    void addSpecs(List<ExperimentSpec> expectedSpecs);

}
