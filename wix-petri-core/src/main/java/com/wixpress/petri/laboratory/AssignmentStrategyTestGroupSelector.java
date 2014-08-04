package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;

import java.util.UUID;

/**
 * @author sagyr
 * @since 9/3/13
 */
public class AssignmentStrategyTestGroupSelector implements TestGroupSelector {
    @Override
    public TestGroup forAnonymousUsers(Experiment experiment) {
        return select(experiment, new AnonymousTestGroupAssignmentStrategy(), null);
    }

    @Override
    public TestGroup forWixUser(Experiment experiment, UUID userId) {
        return select(experiment, new GuidTestGroupAssignmentStrategy(), userId.toString());
    }

    private TestGroup select(Experiment experiment, TestGroupAssignmentStrategy assignmentStrategy, String userId) {
        return assignmentStrategy.getAssignment(experiment, userId);
    }
}
