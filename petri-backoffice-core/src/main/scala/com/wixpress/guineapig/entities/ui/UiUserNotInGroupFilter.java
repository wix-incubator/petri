package com.wixpress.guineapig.entities.ui;

/**
 * Created by dalias on 8/9/2014.
 */

import com.google.auto.value.AutoValue;

import java.util.List;

/** Javadoc. (In real life, it would be on the methods too.) */
@AutoValue
public abstract class UiUserNotInGroupFilter {
    public static UiUserNotInGroupFilter create(List<String> excludeUserGroups) {
        return new AutoValue_UiUserNotInGroupFilter(excludeUserGroups);
    }

    public abstract List<String> excludeUserGroups();
}