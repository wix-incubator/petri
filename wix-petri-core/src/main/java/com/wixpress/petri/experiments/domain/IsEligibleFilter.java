package com.wixpress.petri.experiments.domain;

import com.google.common.base.Predicate;
import com.wixpress.petri.laboratory.UserInfo;

import javax.annotation.Nullable;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 3/9/14
* Time: 4:07 PM
* To change this template use File | Settings | File Templates.
*/
public class IsEligibleFilter implements Predicate<Filter> {
    private final UserInfo userInfo;
    private final Experiment experiment;

    IsEligibleFilter(UserInfo userInfo, Experiment experiment) {
        this.userInfo = userInfo;
        this.experiment = experiment;
    }

    public static IsEligibleFilter isEligibleFor(UserInfo userInfo, Experiment experiment) {
        return new IsEligibleFilter(userInfo, experiment);
    }

    @Override
    public boolean apply(@Nullable Filter filter) {
        return filter.isEligible(userInfo, experiment);
    }
}
