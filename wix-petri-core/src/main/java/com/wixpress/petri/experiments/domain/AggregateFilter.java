package com.wixpress.petri.experiments.domain;

import com.google.common.base.Predicate;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.tryFind;
import static com.google.common.collect.Lists.newArrayList;
import static com.wixpress.petri.experiments.domain.AggregateFilter.IsNotFilter.isNotFilter;


/**
 * @author: talyag
 * @since: 5/16/14
 */
public class AggregateFilter implements Filter {
    private final Filter aggregate;
    private final List<Filter> filters;

    public AggregateFilter(List<Filter> filters) {
        this.filters = filters;
        this.aggregate = aggregate();
    }

    @Override
    public boolean isEligible(FilterEligibility filterEligibility) {
        return aggregate.isEligible(filterEligibility);
    }

    private Filter aggregate() {
        if (filters.isEmpty())
            return new NoFilter();

        return new AndFilter(excludeUidFilter(), atLeastOneInclusiveOrAllExclusives());
    }

    private Filter atLeastOneInclusiveOrAllExclusives() {
        ArrayList<Filter> inclusiveFilters = inclusiveFilters();
        ArrayList<Filter> exclusiveFilters = exclusiveFilters();

        if (inclusiveFilters.isEmpty() && exclusiveFilters.isEmpty())
            return new NoFilter();
        else {
            Filter inclusiveFilter = new OrFilter(inclusiveFilters.toArray(new Filter[inclusiveFilters.size()]));
            Filter exclusiveFilter = exclusiveFilters.isEmpty() ? new ConstantFilter(false) : new AndFilter(exclusiveFilters.toArray(new Filter[exclusiveFilters.size()]));
            return new OrFilter(inclusiveFilter, exclusiveFilter);
        }
    }

    private Filter excludeUidFilter() {
        return tryFind(filters, new IsExcludeUidFilter()).or(new NoFilter());
    }

    private ArrayList<Filter> inclusiveFilters() {
        return newArrayList(filter(filters, new IsInclusiveFilter()));
    }

    private ArrayList<Filter> exclusiveFilters() {
        return newArrayList(filter(filters, new IsExclusiveFilter()));
    }

    private class IsExclusiveFilter implements Predicate<Filter> {

        @Override
        public boolean apply(Filter input) {
            //TODO - solve nicer than 'not special type'
            return !(new IsInclusiveFilter().apply(input) || new IsExcludeUidFilter().apply(input));
        }
    }

    private class IsInclusiveFilter implements Predicate<Filter> {

        @Override
        public boolean apply(Filter input) {
            return input instanceof WixEmployeesFilter || input instanceof IncludeUserIdsFilter;
        }
    }

    public static class IsExcludeUidFilter implements Predicate<Filter> {

        @Override
        public boolean apply(Filter input) {
            return isNotFilter(IncludeUserIdsFilter.class).apply(input);
        }
    }

    public static class IsNotFilter implements Predicate<Filter> {

        Type T;

        public static IsNotFilter isNotFilter(Type T) {
            return new IsNotFilter().withType(T);
        }

        private IsNotFilter withType(Type T) {
            this.T = T;
            return this;
        }

        @Override
        public boolean apply(Filter input) {
            return input instanceof NotFilter && T.equals(((NotFilter) input).getInternal().getClass());
        }
    }
}
