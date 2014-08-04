package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wixpress.petri.laboratory.UserInfo;

/**
 * Created with IntelliJ IDEA.
 * User: uri
 * Date: 4/27/14
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class NotFilter implements Filter {


    private final Filter internal;


    @JsonCreator
    public NotFilter(@JsonProperty(value = "internal") Filter internal) {
        this.internal = internal;
    }

    public Filter getInternal() {
        return internal;
    }

    @Override
    public boolean isEligible(UserInfo user, Experiment experiment) {
        return !internal.isEligible(user, experiment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotFilter notFilter = (NotFilter) o;

        if (internal != null ? !internal.equals(notFilter.internal) : notFilter.internal != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return internal != null ? internal.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "NotFilter{" +
                "internal=" + internal +
                '}';
    }


}
