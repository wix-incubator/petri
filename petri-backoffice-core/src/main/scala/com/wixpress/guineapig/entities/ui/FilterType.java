package com.wixpress.guineapig.entities.ui;

/**
 * Created with IntelliJ IDEA.
 * User: avgarm
 * Date: 12/9/13
 * Time: 4:00 PM
 * To change this template use File | Settings | File Templates.
 */
public enum FilterType {
    FIRST_TIME_ANON_USERS("First Time Anonymous Visitors"),
    REGISTERED_USERS("All Registered Users"),
    NON_REGISTERED_USERS("No Existing Registered Users"),
    NEW_USERS("New Registered Users"),
    USERS_NONE("No Filter On Users"),
    USERS_SPECIFIC("Specific Users Only"),
    WIX_USERS("wixUsers"),
    INCLUDE_GEO("geo"),
    EXCLUDE_GEO("geo"),
    LANGUAGE("language"),
    HOST("hosts"),
    ARTIFACT("artifacts"),
    INCLUDE_ID("include guids"),
    EXCLUDE_ID("exclude guids"),
    USER_AGENT_REGEX("User Agent Regex"),
    USER_NOT_IN_ANY_GROUP("User not in any group");

    private String type;

    private FilterType(String s) {
        type = s;
    }

    public String getType() {
        return type;
    }

}