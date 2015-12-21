package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;

/**
 * @author alex
 * @since 8/21/11 5:12 PM
 */

public abstract class TestGroupAssignmentStrategy {

    public TestGroup getAssignment(Experiment experiment, String kernel) {
        long toss = Math.abs((long)getToss(experiment, kernel));
        double hashed = 100.0 * toss / Integer.MAX_VALUE;
        int chunk = (int) hashed;
        return experiment.getTestGroupByChunk(chunk);
    }

    protected abstract int getToss(Experiment experiment, String kernel);
}
