package com.wixpress.guineapig.entities.ui;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

@JsonDeserialize(builder = UiSpecForScopeBuilder.class)
public class UiSpecForScope {
    final private String key;
    final private long startDate;
    final private long endtDate;
    final private String scope;
    final private List<UiTestGroup> groups;
    final private String exposureId;

    final private boolean forRegisteredUsers;

    public UiSpecForScope(String key, List<UiTestGroup> groups, String scope, long startDate, long endDate, boolean forRegisteredUsers, String exposureId) {

        this.key = key;
        this.groups = groups;
        this.scope = scope;
        this.startDate = startDate;
        this.endtDate = endDate;
        this.forRegisteredUsers = forRegisteredUsers;
        this.exposureId = exposureId;
    }

    public boolean isForRegisteredUsers() {
        return forRegisteredUsers;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getEndtDate() {
        return endtDate;
    }

    public String getScope() {
        return scope;
    }

    public String getKey() {
        return key;
    }

    public List<UiTestGroup> getGroups() {
        return groups;
    }

    public String getExposureId() {
        return exposureId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UiSpecForScope)) return false;

        UiSpecForScope that = (UiSpecForScope) o;

        if (endtDate != that.endtDate) return false;
        if (forRegisteredUsers != that.forRegisteredUsers) return false;
        if (startDate != that.startDate) return false;
        if (exposureId != null ? !exposureId.equals(that.exposureId) : that.exposureId != null) return false;
        if (groups != null ? !groups.equals(that.groups) : that.groups != null) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (scope != null ? !scope.equals(that.scope) : that.scope != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (int) (startDate ^ (startDate >>> 32));
        result = 31 * result + (int) (endtDate ^ (endtDate >>> 32));
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        result = 31 * result + (groups != null ? groups.hashCode() : 0);
        result = 31 * result + (exposureId != null ? exposureId.hashCode() : 0);
        result = 31 * result + (forRegisteredUsers ? 1 : 0);
        return result;
    }
}
