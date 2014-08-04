package com.wixpress.petri.laboratory;

import java.util.Random;

/**
 * @author alex
 * @since 8/22/11 4:41 PM
 */

public class AnonymousTestGroupAssignmentStrategy extends TestGroupAssignmentStrategy {

    Random random = new Random();

    @Override
    protected int getToss(com.wixpress.petri.experiments.domain.Experiment experiment, String kernel) {
        return random.nextInt(10000);
    }
}
