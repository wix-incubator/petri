package com.wixpress.guineapig.entities.ui;

/**
 * Created by avgarm on 7/8/2014.
 */

import com.google.auto.value.AutoValue;

import java.util.List;

/** Javadoc. (In real life, it would be on the methods too.) */
@AutoValue
public abstract class UiGeoFilter {
    public static UiGeoFilter create(boolean isExclude, List<String> geo) {
        return new AutoValue_UiGeoFilter(isExclude, geo);
    }
    public abstract boolean isExclude();
    public abstract List<String> geo();
}