package com.wixpress.petri.experiments.domain;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class UnrecognizedFilter implements Filter {

    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
