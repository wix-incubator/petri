package com.wixpress.petri.laboratory;

import com.wixpress.petri.PetriRPCClient;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.petri.PetriClient;

import java.net.MalformedURLException;
import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 9/2/14
* Time: 12:17 PM
* To change this template use File | Settings | File Templates.
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
