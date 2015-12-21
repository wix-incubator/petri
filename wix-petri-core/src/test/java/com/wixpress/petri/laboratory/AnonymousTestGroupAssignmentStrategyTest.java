package com.wixpress.petri.laboratory;


/**
 * Created by talyag on 15/9/15.
 */
public class AnonymousTestGroupAssignmentStrategyTest extends AbstractTestGroupAssignmentStrategyTest {

    @Override
    TestGroupAssignmentStrategy strategy() {
        return new AnonymousTestGroupAssignmentStrategy();
    }

}

