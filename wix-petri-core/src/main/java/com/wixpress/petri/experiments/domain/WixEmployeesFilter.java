package com.wixpress.petri.experiments.domain;

/**
 * @author: talyag
 * @since: 11/26/13
 */
public class WixEmployeesFilter implements Filter {


    public WixEmployeesFilter() {
    }

    @Override
    public String toString() {
        return "WixUserFilter";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override
    public boolean isEligible(FilterEligibility filterEligibility) {
        String email = filterEligibility.getEmail();
        return email != null && email.endsWith("@wix.com");
    }
}
