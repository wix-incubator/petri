package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.UserInfo;

/**
 * @author: talyag
 * @since: 5/25/14
 */
public class UnrecognizedFilter implements Filter {

    @Override
    public boolean isEligible(UserInfo user, Experiment experiment) {
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
