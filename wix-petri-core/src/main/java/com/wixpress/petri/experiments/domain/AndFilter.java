package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.UserInfo;

import java.util.List;

import static com.google.common.collect.Iterables.all;
import static com.wixpress.petri.experiments.domain.IsEligibleFilter.isEligibleFor;
import static java.util.Arrays.asList;

/**
 * @author: talyag
 * @since: 12/11/13
 */
public class AndFilter implements Filter {

    private final List<Filter> filters;

    public AndFilter(final Filter... filter) {
        this.filters = asList(filter);
    }

    @Override
    public boolean isEligible(UserInfo userInfo, Experiment experiment) {
        return all(filters, isEligibleFor(userInfo, experiment));
    }

}
