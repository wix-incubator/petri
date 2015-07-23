package com.wixpress.petri.experiments.domain;

import org.joda.time.DateTime;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class NewUsersFilter implements Filter {


    public NewUsersFilter() {
    }

    @Override
    public String toString() {
        return "NewUsersFilter";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        DateTime userCreationDate = eligibilityCriteria.getUserCreationDate();
        return userCreationDate != null && userCreationDate.isAfter(eligibilityCriteria.getExperimentStartDate());
    }

}
