package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class HostFilter implements Filter {

    private List<String> hosts;

    public List<String> getHosts() {
        return hosts;
    }

    @JsonCreator
    public HostFilter(@JsonProperty("hosts") List<String> hosts) {
        this.hosts = hosts;
    }

    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        return hosts.contains(eligibilityCriteria.getHost());
    }

    @Override
    public String toString() {
        return "HostFilter{" +
                "hosts=" + hosts +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HostFilter that = (HostFilter) o;

        if (hosts != null ? !hosts.equals(that.hosts) : that.hosts != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hosts != null ? hosts.hashCode() : 0;
    }
}
