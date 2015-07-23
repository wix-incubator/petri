package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.petri.PetriClient;

import java.net.MalformedURLException;
import java.util.List;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class PetriClientExperimentSource implements CachedExperiments.ExperimentsSource {
    private final PetriClient petriProxy;

    public PetriClientExperimentSource(PetriClient petriProxy) throws MalformedURLException {
        this.petriProxy = petriProxy;
    }

    @Override
    public List<Experiment> read() {
        return petriProxy.fetchActiveExperiments();
    }

    @Override
    public boolean isUpToDate() {
        return true;
    }
}
