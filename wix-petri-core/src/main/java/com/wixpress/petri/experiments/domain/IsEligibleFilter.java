package com.wixpress.petri.experiments.domain;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 3/9/14
 * Time: 4:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class IsEligibleFilter implements Predicate<Filter> {
    private final FilterEligibility filterEligibility;

    IsEligibleFilter(FilterEligibility filterEligibility) {
        this.filterEligibility = filterEligibility;
    }

    public static IsEligibleFilter isEligibleFor(FilterEligibility filterEligibility) {
        return new IsEligibleFilter(filterEligibility);
    }

    @Override
    public boolean apply(@Nullable Filter filter) {
        return filter.isEligible(filterEligibility);
    }
}
