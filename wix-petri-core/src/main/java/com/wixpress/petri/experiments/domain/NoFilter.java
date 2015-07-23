package com.wixpress.petri.experiments.domain;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class NoFilter implements Filter {
    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        return true;
    }
}
