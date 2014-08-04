package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.UserInfo;

/**
 * @author: talyag
 * @since: 11/26/13
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
    public boolean isEligible(UserInfo user, Experiment experiment) {
        return user.dateCreated != null && user.dateCreated.isAfter(experiment.getStartDate());
    }
}
