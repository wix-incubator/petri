package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Assignment;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public interface TestGroupAssignmentTracker {

    void newAssignment(Assignment assignment);
}
