package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;

import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 9/3/13
 * Time: 12:37 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TestGroupSelector {
    TestGroup forAnonymousUsers(Experiment experiment);

    TestGroup forWixUser(Experiment experiment, UUID userId);
}
