package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Assignment;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 8/13/13
 * Time: 11:34 AM
 * To change this template use File | Settings | File Templates.
 */
public interface TestGroupAssignmentTracker {

    void newAssignment(Assignment assignment);
}
