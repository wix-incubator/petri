package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class ScopeDefinition {

    private final String name;
    private final boolean onlyForLoggedInUsers;

    /**
     * Use this if the experiment is always conducted for logged in users - i.e in the dashboard, editor etc. (e.g WixEmployees filter)
     */
    public static ScopeDefinition aScopeDefinitionOnlyForLoggedInUsers(String scope) {
        return new ScopeDefinition(scope, true);
    }

    /** Use this If the experiment can be conducted for non-logged in users - i.e in the renderer, template-viewer etc.
     */
    public static ScopeDefinition aScopeDefinitionForAllUserTypes(String scope) {
        return new ScopeDefinition(scope, false);
    }

    @JsonCreator
    public ScopeDefinition(@JsonProperty(value = "name") String name,
                           @JsonProperty(value = "onlyForLoggedInUsers") boolean onlyForLoggedInUsers) {
        this.name = name;
        this.onlyForLoggedInUsers = onlyForLoggedInUsers;
    }

    public boolean isOnlyForLoggedInUsers() {
        return onlyForLoggedInUsers;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "ScopeDefinition{" +
                "name='" + name + '\'' +
                ", onlyForLoggedInUsers=" + onlyForLoggedInUsers +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScopeDefinition that = (ScopeDefinition) o;

        if (onlyForLoggedInUsers != that.onlyForLoggedInUsers) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (onlyForLoggedInUsers ? 1 : 0);
        return result;
    }
}
