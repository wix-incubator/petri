package com.wixpress.guineapig.dsl;

import com.wixpress.guineapig.entities.ui.UiTestGroup;
import com.wixpress.petri.experiments.domain.TestGroup;

import java.util.Arrays;
import java.util.List;

public class TestGroupMakers {

    public static final List<UiTestGroup> DEFAULT_UI_TEST_GROUPS =
            Arrays.asList(new UiTestGroup(1, "A", 50), new UiTestGroup(2, "B", 50));

    public static final List<TestGroup> TEST_GROUPS_FOR_CLIENT_WITH_OLD_WINNING =
            Arrays.asList(new TestGroup(1, 100, "old"), new TestGroup(2, 0, "new"));

    public static final List<TestGroup> TEST_GROUPS_FOR_CLIENT_WITH_NEW_WINNING =
            Arrays.asList(new TestGroup(1, 0, "old"), new TestGroup(2, 100, "new"));
}
