package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.EligibilityCriteria;
import com.wixpress.petri.experiments.domain.Filter;

import static java.lang.Thread.sleep;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class SlowFilter implements Filter {
    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        try {
            sleep(TrackableLaboratoryTest.EXPERIMENT_MAX_TIME_MILLIS + 1);
        } catch (InterruptedException e) {
        }
        return true;

    }
}
