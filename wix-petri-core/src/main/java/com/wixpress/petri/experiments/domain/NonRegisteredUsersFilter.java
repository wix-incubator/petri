package com.wixpress.petri.experiments.domain;

public class NonRegisteredUsersFilter implements Filter {


    public NonRegisteredUsersFilter() {
    }

    @Override
    public String toString() {
        return "NonRegisteredUsersFilter";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        return !eligibilityCriteria.isRegisteredUserExists();
    }

}
