package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.EligibilityCriteria;
import com.wixpress.petri.experiments.domain.Filter;

import static java.lang.Thread.sleep;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 7/16/14
 * Time: 4:29 PM
 * To change this template use File | Settings | File Templates.
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
