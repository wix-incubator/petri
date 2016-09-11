package com.wixpress.guineapig.entities.ui;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Created by avgarm on 5/21/2014.
 */
@JsonDeserialize(builder = UiFilterBuilder.class)
public class UiFilter {

    final public static UiFilter uiFilterMainFilterOnNonRegistered = new UiFilterBuilder()
            .withFilterName("mainFilter")
            .withEnabled(true)
            .withOptionalValue(asList(MainFilter.FILTERS.getType(),MainFilter.OPEN_TO_ALL.getType()))
            .build();

    final public static UiFilter uiFilterMainFilterOnRegistered = new UiFilterBuilder()
            .withFilterName("mainFilter")
            .withEnabled(true)
            .withOptionalValue(asList(MainFilter.WIX_USERS_ONLY.getType(), MainFilter.FILTERS.getType(), MainFilter.OPEN_TO_ALL.getType()))
            .build();

    final public static UiFilter uiFilterNonWixUsers = new UiFilterBuilder()
            .withFilterName("wixUsers")
            .withEnabled(false)
            .withMandatoryValue(asList("false"))
            .build();

    final public static UiFilter uiFilterUsersForRegistered = new UiFilterBuilder()
            .withFilterName("users")
            .withEnabled(true)
            .withOptionalValue(asList(FilterType.NEW_USERS.getType(), FilterType.REGISTERED_USERS.getType(),FilterType.USERS_SPECIFIC.getType()))
            .build();

    final public static UiFilter uiFilterExcludeSpecificUsers = new UiFilterBuilder()
            .withFilterName("guids")
            .withEnabled(true)
            .withOptionalValue(Arrays.asList(FilterType.EXCLUDE_ID.getType()))
            .build();

    final public static UiFilter uiFilterUsersForNotRegistered = new UiFilterBuilder()
            .withFilterName("users")
            .withEnabled(true)
            .withOptionalValue(asList(FilterType.USERS_NONE.getType(), FilterType.FIRST_TIME_ANON_USERS.getType(), FilterType.NON_REGISTERED_USERS.getType()))
            .build();

    final public static UiFilter uiFilterNoGuids = new UiFilterBuilder()
            .withFilterName("guids")
            .withEnabled(false)
            .build();

    final private String filterName;
    final private boolean enabled;
    final private List<String> mandatoryValue;
    final private List<String> optionalValue;

    public String getFilterName() {
        return filterName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Object getMandatoryValue() {
        return mandatoryValue;
    }

    public Object getOptionalValue() {
        return optionalValue;
    }

    public UiFilter(String filterName, boolean enabled, List<String> mandatoryValue, List<String> optionalValue) {
        this.filterName = filterName;
        this.enabled = enabled;
        this.mandatoryValue = mandatoryValue;
        this.optionalValue = optionalValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UiFilter that = (UiFilter) o;

        if (enabled != that.enabled) return false;
        if (!filterName.equals(that.filterName)) return false;
        if (!mandatoryValue.equals(that.mandatoryValue)) return false;
        if (!optionalValue.equals(that.optionalValue)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = filterName.hashCode();
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + mandatoryValue.hashCode();
        result = 31 * result + optionalValue.hashCode();
        return result;
    }


}
