package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Arrays.asList;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class IncludeUserIdsFilter implements Filter {

    private final List<UUID> userGuids;

    @JsonCreator
    public IncludeUserIdsFilter(@JsonProperty("ids") UUID... userGuids) {
        this.userGuids = (userGuids == null) ? new ArrayList<UUID>() : asList(userGuids);
    }

    public List<UUID> getIds() {
        return new ArrayList<>(userGuids);
    }

    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        return userGuids.contains(eligibilityCriteria.getUserId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IncludeUserIdsFilter that = (IncludeUserIdsFilter) o;

        if (userGuids != null ? !userGuids.equals(that.userGuids) : that.userGuids != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return userGuids != null ? userGuids.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "IncludeUserIdsFilter{" +
                "userGuids=" + userGuids +
                '}';
    }

}
