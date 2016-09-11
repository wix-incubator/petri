package com.wixpress.guineapig.entities.ui;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UiSpecForScopeBuilder {

    private final static Long INVALID_TIME = -1L;

    private List<UiTestGroup> groups = new ArrayList<>();
    private String key = "";
    private long startDate = INVALID_TIME;
    private long endDate = INVALID_TIME;
    private String scope = "";
    private String exposureId = null;
    private boolean forRegisteredUsers = false;


    public static UiSpecForScopeBuilder anUiSpec() {
        return new UiSpecForScopeBuilder();
    }

    public UiSpecForScopeBuilder withKey(String key) {
        this.key = key;
        return this;
    }

    public UiSpecForScopeBuilder withGroups(List<UiTestGroup> groups) {
        this.groups = groups;
        return this;
    }

    public UiSpecForScopeBuilder withScope(String scope) {
        this.scope = scope;
        return this;
    }

    public UiSpecForScopeBuilder withStartDate(long startDate) {
        this.startDate = startDate;
        return this;
    }

    public UiSpecForScopeBuilder withEndDate(long endtDate) {
        this.endDate = endtDate;
        return this;
    }

    public UiSpecForScopeBuilder withForRegisteredUsers(boolean forRegisteredUsers) {
        this.forRegisteredUsers = forRegisteredUsers;
        return this;
    }

    public UiSpecForScopeBuilder withExposureId(String exposureId) {
        this.exposureId = exposureId;
        return this;
    }

    public UiSpecForScope build() {
        return new UiSpecForScope(key, groups, scope, startDate, endDate, forRegisteredUsers, exposureId);
    }
}
