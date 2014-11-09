package com.wixpress.petri.experiments.domain;

/**
 * @author talyag
 * @since 9/9/14
 */
public class AdditionalFilter implements Filter {

    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }
}
