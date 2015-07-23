package com.wixpress.petri.experiments.domain;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
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
