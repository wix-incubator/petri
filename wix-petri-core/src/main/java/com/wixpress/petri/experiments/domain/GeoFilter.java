package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wixpress.petri.laboratory.UserInfo;

import java.util.List;

/**
 * @author: talyag
 * @since: 11/26/13
 */
public class GeoFilter implements Filter {

    private List<String> countries;

    public List<String> getCountries() {
        return countries;
    }

    @JsonCreator
    public GeoFilter(@JsonProperty("countries") List<String> countries) {
        this.countries = countries;
    }

    @Override
    public boolean isEligible(UserInfo user, Experiment experiment) {
        return countries.contains(user.country);
    }

    @Override
    public String toString() {
        return "GeoFilter{" +
                "geos=" + countries +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GeoFilter geoFilter = (GeoFilter) o;

        if (countries != null ? !countries.equals(geoFilter.countries) : geoFilter.countries != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return countries != null ? countries.hashCode() : 0;
    }
}
