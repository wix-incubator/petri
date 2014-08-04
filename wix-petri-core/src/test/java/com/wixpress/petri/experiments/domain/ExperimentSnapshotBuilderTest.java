package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.dsl.TestGroupMakers;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.anExperimentSnapshot;
import static com.wixpress.petri.laboratory.dsl.TestGroupMakers.TestGroup;
import static com.wixpress.petri.laboratory.dsl.TestGroupMakers.*;
import static java.util.Arrays.asList;

public class ExperimentSnapshotBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void throwsWhenEndDateIsGreaterThanStartDate() throws Exception {
        DateTime startDate = new DateTime();
        DateTime endDate = startDate.minusSeconds(1);
        anExperimentSnapshot().withStartDate(startDate).withEndDate(endDate).withGroups(VALID_TEST_GROUP_LIST).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGroupChunksMustTotal100() {
        List<TestGroup> testGroupsNotAddingTo100 = asList(
                a(TestGroupMakers.TestGroup,
                        with(TestGroupMakers.groupId, 1)).
                        make(),
                a(TestGroupMakers.TestGroup,
                        with(TestGroupMakers.groupId, 2)).
                        make()
        );
        anExperimentSnapshot().withGroups(testGroupsNotAddingTo100).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsWhenFeatureToggleButNotOneWinningGroup() {
        List<TestGroup> nonToggleTestGroups = asList(
                a(TestGroup,
                        with(probability, 50)).
                        make(),
                a(TestGroup,
                        with(probability, 50)).
                        make()
        );
        anExperimentSnapshot().withGroups(nonToggleTestGroups).withFeatureToggle(true).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsWhenOnlyForLoggedInIsNull() {
        anExperimentSnapshot().withGroups(VALID_TEST_GROUP_LIST).build();
    }
}
