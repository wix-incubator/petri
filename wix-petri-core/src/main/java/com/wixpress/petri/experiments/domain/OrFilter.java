package com.wixpress.petri.experiments.domain;

import java.util.List;

import static com.google.common.collect.Iterables.any;
import static com.wixpress.petri.experiments.domain.IsEligibleFilter.isEligibleFor;
import static java.util.Arrays.asList;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class OrFilter implements Filter {

    private final List<Filter> filters;

    public OrFilter(final Filter... filter) {
        this.filters = asList(filter);
    }

    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        return any(filters, isEligibleFor(eligibilityCriteria));
    }

}
