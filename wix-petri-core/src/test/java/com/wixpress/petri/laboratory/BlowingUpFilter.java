package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.EligibilityCriteria;
import com.wixpress.petri.experiments.domain.Filter;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class BlowingUpFilter implements Filter {
    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        throw new FilterExploded();
    }

    static public class FilterExploded extends RuntimeException {

    }
}
