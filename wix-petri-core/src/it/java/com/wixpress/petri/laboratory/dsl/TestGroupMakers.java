package com.wixpress.petri.laboratory.dsl;

import com.natpryce.makeiteasy.Instantiator;
import com.natpryce.makeiteasy.Property;
import com.natpryce.makeiteasy.PropertyLookup;
import com.wixpress.petri.experiments.domain.TestGroup;

import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.natpryce.makeiteasy.Property.newProperty;
import static java.util.Arrays.asList;

/**
 * @author sagyr
 * @since 8/6/13
 */
public class TestGroupMakers {
    public static final Property<TestGroup, String> value = newProperty();
    public static final Property<TestGroup, Integer> groupId = newProperty();
    public static final Property<TestGroup, Integer> probability = newProperty();


    public static final Instantiator<TestGroup> TestGroup = new Instantiator<TestGroup>() {

        @Override
        public TestGroup instantiate(PropertyLookup<TestGroup> lookup) {
            TestGroup result = new TestGroup();
            result.setChunk(lookup.valueOf(probability, 0));
            result.setId(lookup.valueOf(groupId, 0));
            result.setValue(lookup.valueOf(value, ""));
            return result;
        }
    };

    public static final String LOSING_VALUE = "OTHER_VALUE";
    public static final String WINNING_VALUE = "THE_VALUE";

    public static final List<TestGroup> TEST_GROUPS_WITH_FIRST_ALWAYS_WINNING = asList(
            a(TestGroup,
                    with(probability, 100),
                    with(value, WINNING_VALUE)).make(),
            a(TestGroup,
                    with(probability, 0),
                    with(value, LOSING_VALUE)).make());

    public static final List<TestGroup> TEST_GROUPS_WITH_SECOND_ALWAYS_WINNING = asList(
            a(TestGroup,
                    with(probability, 0),
                    with(value, WINNING_VALUE)).make(),
            a(TestGroup,
                    with(probability, 100),
                    with(value, LOSING_VALUE)).make());

    public static final List<TestGroup> VALID_TEST_GROUP_LIST = asList(
            a(TestGroupMakers.TestGroup,
                    with(TestGroupMakers.groupId, 1),
                    with(value, "g1"),
                    with(probability, 50)).
                    make(),
            a(TestGroupMakers.TestGroup,
                    with(TestGroupMakers.groupId, 2),
                    with(value, "g2"),
                    with(probability, 50)).
                    make());

    public static final List<TestGroup> VALID_TEST_GROUP_WITH_NO_VALUES_LIST = asList(
            a(TestGroupMakers.TestGroup,
                    with(TestGroupMakers.groupId, 1),
                    with(probability, 50)).
                    make(),
            a(TestGroupMakers.TestGroup,
                    with(TestGroupMakers.groupId, 2),
                    with(probability, 50)).
                    make());
}
