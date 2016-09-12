package com.wixpress.guineapig.util;

import com.wixpress.guineapig.services.ExperimentEvent;
import com.wixpress.petri.experiments.domain.ExperimentSnapshot;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;

public class Matchers {


    public static Matcher<ExperimentEvent> isForAction(String action) {
        return hasProperty("action", is(action));
    }

    public static Matcher<? super ExperimentSnapshot> snapshotHasKey(final String key) {
        return new TypeSafeMatcher<ExperimentSnapshot>() {
            @Override
            protected boolean matchesSafely(ExperimentSnapshot item) {
                return key.equals(item.key());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("An experiment with key [" + key + "]");
            }
        };
    }
}
