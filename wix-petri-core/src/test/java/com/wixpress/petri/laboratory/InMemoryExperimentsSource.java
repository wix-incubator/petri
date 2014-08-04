package com.wixpress.petri.laboratory;

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
    private boolean isUpToDate = false;

    @Override
    public List<Experiment> read() {
        return this.experiments;
    }

    @Override
    public boolean isUpToDate() {
        return isUpToDate;
    }

    public void write(List<Experiment> experiments) {
        this.experiments = experiments;
        this.isUpToDate = true;
    }

    public void setIsUpToDate(boolean isUpToDate) {
        this.isUpToDate = isUpToDate;
    }
}
