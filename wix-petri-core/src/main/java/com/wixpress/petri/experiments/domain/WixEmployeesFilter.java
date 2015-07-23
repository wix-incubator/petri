package com.wixpress.petri.experiments.domain;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class WixEmployeesFilter implements Filter {


    public WixEmployeesFilter() {
    }

    @Override
    public String toString() {
        return "WixEmployeesFilter";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        String email = eligibilityCriteria.getEmail();
        return email != null && email.endsWith("@wix.com");
    }
}
