package com.wixpress.petri.laboratory;

import java.util.Random;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */

public class AnonymousTestGroupAssignmentStrategy extends TestGroupAssignmentStrategy {

    Random random = new Random();

    @Override
    protected int getToss(com.wixpress.petri.experiments.domain.Experiment experiment, String kernel) {
        return random.nextInt(10000);
    }
}
