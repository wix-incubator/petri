package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public abstract class TestGroupAssignmentStrategy {

    public TestGroup getAssignment(Experiment experiment, String kernel) {
        int toss = getToss(experiment, kernel);
        int chunk = (toss % 100);
        return experiment.getTestGroupByChunk(chunk);
    }

    protected abstract int getToss(Experiment experiment, String kernel);
}
