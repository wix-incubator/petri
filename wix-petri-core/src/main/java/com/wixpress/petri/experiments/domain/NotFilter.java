package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
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
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        return !internal.isEligible(eligibilityCriteria);
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
