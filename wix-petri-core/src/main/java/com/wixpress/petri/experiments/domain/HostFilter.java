package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wixpress.petri.laboratory.UserInfo;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: itayk
 * Date: 22/07/14
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
    public boolean isEligible(UserInfo user, Experiment experiment) {
        return hosts.contains(user.host);
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
