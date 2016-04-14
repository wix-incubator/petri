package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;

/**
 * @author sagyr
 * @since 8/8/13
 */
public class ExpiredExperiments implements ExperimentsLog.Predicate {
    private Experiments experiments;
    private boolean isRemoveFTCookiesEnabledByFT;

    public ExpiredExperiments(Experiments experiments, boolean isRemoveFTCookiesEnabledByFT) {
        this.experiments = experiments;
        this.isRemoveFTCookiesEnabledByFT = isRemoveFTCookiesEnabledByFT;
    }

    @Override
    public boolean matches(int experimentId) {
        if (experiments.staleOrEmpty())
            return false;

        Experiment experiment = experiments.findById(experimentId);
        return (experiment == null) || experiment.isTerminated() || (isRemoveFTCookiesEnabledByFT && experiment.isToggle());
    }

}
