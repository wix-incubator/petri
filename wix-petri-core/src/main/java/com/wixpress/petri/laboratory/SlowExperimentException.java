package com.wixpress.petri.laboratory;


import com.wixpress.petri.experiments.domain.Experiment;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */

public class SlowExperimentException extends RuntimeException {

    public SlowExperimentException(Experiment experiment) {
        super("Slow Conducting time of experiment : " + experiment);
    }
}
