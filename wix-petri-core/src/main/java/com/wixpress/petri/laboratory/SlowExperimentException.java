package com.wixpress.petri.laboratory;


import com.wixpress.petri.experiments.domain.Experiment;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 6/24/14
* Time: 6:47 PM
* To change this template use File | Settings | File Templates.
*/

public class SlowExperimentException extends RuntimeException {

    public SlowExperimentException(Experiment experiment) {
        super("Slow Conducting time of experiment : " + experiment);
    }
}
