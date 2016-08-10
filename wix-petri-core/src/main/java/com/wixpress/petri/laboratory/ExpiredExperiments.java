package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;

/**
 * @author sagyr
 * @since 8/8/13
 */
public class ExpiredExperiments implements ExperimentsLog.Predicate {
    private Experiments experiments;

    public ExpiredExperiments(Experiments experiments) {
        this.experiments = experiments;
    }

    @Override
    public boolean matches(int experimentId) {
        if (experiments.staleOrEmpty())
            return false;

        Experiment experiment = experiments.findById(experimentId);
        return (experiment == null) || experiment.isTerminated() || experiment.isToggle();
    }

}
