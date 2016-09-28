package com.wixpress.guineapig.dsl;

import com.wixpress.guineapig.entities.ui.UiTestGroup;

import java.util.Arrays;
import java.util.List;

public class TestGroupMakers {

    public static final List<UiTestGroup> DEFAULT_UI_TEST_GROUPS =
            Arrays.asList(new UiTestGroup(1, "A", 50), new UiTestGroup(2, "B", 50));

    public static final List<UiTestGroup> UI_TEST_GROUPS_FOR_CLIENT_WITH_NEW_WINNING =
            Arrays.asList(new UiTestGroup(1, "old", 0), new UiTestGroup(2, "new", 100));
}
