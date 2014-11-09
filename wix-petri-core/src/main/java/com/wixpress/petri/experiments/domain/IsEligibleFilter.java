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
    private final EligibilityCriteria eligibilityCriteria;

    IsEligibleFilter(EligibilityCriteria eligibilityCriteria) {
        this.eligibilityCriteria = eligibilityCriteria;
    }

    public static IsEligibleFilter isEligibleFor(EligibilityCriteria eligibilityCriteria) {
        return new IsEligibleFilter(eligibilityCriteria);
    }

    @Override
    public boolean apply(@Nullable Filter filter) {
        return filter.isEligible(eligibilityCriteria);
    }
}
