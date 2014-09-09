package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Filter;
import com.wixpress.petri.experiments.domain.FilterEligibility;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 7/16/14
 * Time: 4:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class BlowingUpFilter implements Filter {
    @Override
    public boolean isEligible(FilterEligibility filterEligibility) {
        throw new FilterExploded();
    }

    static public class FilterExploded extends RuntimeException {

    }
}
