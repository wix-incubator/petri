package com.wixpress.petri.experiments.domain;

import com.google.common.base.Predicate;

import java.util.List;

import static com.google.common.collect.Iterables.find;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class FiltersValidator {

    public FiltersValidator() {
    }

    public boolean checkValidity(List<Filter> filters) {
        return find(filters, new Predicate<Filter>() {
            @Override
            public boolean apply(Filter input) {
                return input instanceof UnrecognizedFilter ||
                        (input instanceof NotFilter && ((NotFilter) input).getInternal() instanceof UnrecognizedFilter);
            }
        }, null) == null;
    }

}
