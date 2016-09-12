package com.wixpress.guineapig.entities.ui;

/**
 * Created by dalias on 8/9/2014.
 */

import com.google.auto.value.AutoValue;

import java.util.List;

/** Javadoc. (In real life, it would be on the methods too.) */
@AutoValue
public abstract class UiUserAgentRegexFilter {
    public static UiUserAgentRegexFilter create(List<String> includeUserAgentRegexes, List<String> excludeUserAgentRegexes) {
        return new AutoValue_UiUserAgentRegexFilter(includeUserAgentRegexes, excludeUserAgentRegexes);
    }
    public abstract List<String> includeUserAgentRegexes();
    public abstract List<String> excludeUserAgentRegexes();
}