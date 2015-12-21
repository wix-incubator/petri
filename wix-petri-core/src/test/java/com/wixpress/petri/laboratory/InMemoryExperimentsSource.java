package com.wixpress.petri.laboratory;

import com.wixpress.petri.ExperimentsAndState;
import com.wixpress.petri.experiments.domain.Experiment;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 7/16/14
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class InMemoryExperimentsSource implements CachedExperiments.ExperimentsSource {

    private List<Experiment> experiments;
    private boolean stale = true;

    @Override
    public ExperimentsAndState read(){
        return new ExperimentsAndState(experiments, stale);
    }

    public void write(List<Experiment> experiments) {
        this.experiments = experiments;
        this.stale = false;
    }

    public void setStale(boolean stale) {
        this.stale = stale;
    }
}
