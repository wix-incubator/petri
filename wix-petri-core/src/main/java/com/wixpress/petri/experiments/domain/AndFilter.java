package com.wixpress.petri.experiments.domain;

import java.util.List;

import static com.google.common.collect.Iterables.all;
import static com.wixpress.petri.experiments.domain.IsEligibleFilter.isEligibleFor;
import static java.util.Arrays.asList;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class AndFilter implements Filter {

    private final List<Filter> filters;

    public AndFilter(final Filter... filter) {
        this.filters = asList(filter);
    }

    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        return all(filters, isEligibleFor(eligibilityCriteria));
    }

}
