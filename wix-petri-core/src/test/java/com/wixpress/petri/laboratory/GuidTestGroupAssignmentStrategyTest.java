package com.wixpress.petri.laboratory;


import com.natpryce.makeiteasy.Donor;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import com.wixpress.petri.util.RepeatRule;
import org.junit.Test;

import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.*;
import static com.wixpress.petri.laboratory.dsl.TestGroupMakers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author: talyag
 * @since: 12/31/13
 */
public class GuidTestGroupAssignmentStrategyTest extends AbstractTestGroupAssignmentStrategyTest {

    @Override
    TestGroupAssignmentStrategy strategy() {
        return new GuidTestGroupAssignmentStrategy();
    }

    private static final Experiment experiment = an(ExperimentMakers.Experiment,
            with(id, 1),
            with(testGroups, listOf(
                    a(TestGroup,
                            with(probability, 80),
                            with(value, "OLD")),
                    a(TestGroup,
                            with(probability, 20),
                            with(value, "NEW"))
            ))
    ).make();

    private static Donor<List<TestGroup>> expandedTestGroups = listOf(
            a(TestGroup,
                    with(probability, 20),
                    with(value, "OLD")),
            a(TestGroup,
                    with(probability, 80),
                    with(value, "NEW"))
    );

    private static final Experiment expandedExperimentWithOriginalId = an(ExperimentMakers.Experiment,
            with(id, 2),
            with(originalId, experiment.getId()),
            with(testGroups, expandedTestGroups)
    ).make();

    private static final Experiment expandedExperimentWithLinkedId = an(ExperimentMakers.Experiment,
            with(id, 13),
            with(originalId, 11),
            with(linkedId, experiment.getId()),
            with(testGroups, expandedTestGroups)
    ).make();

    @RepeatRule.Repeat(times = 1000)
    @Test
    public void expandedExperimentCanOnlyReturnHigherTestGroup() throws Exception {
        onlyHigherTestGroup(expandedExperimentWithOriginalId);
    }

    @RepeatRule.Repeat(times = 1000)
    @Test
    public void linkedExperimentCanOnlyReturnHigherTestGroup() throws Exception {
        onlyHigherTestGroup(expandedExperimentWithLinkedId);
    }

    private void onlyHigherTestGroup(Experiment connectedExperiment) {
        // a 'magic number' - an example of a uid that causes the test to fail if linked_id is ignored
        // (specifically for the testgroups with 20-80)
        // UserGuid.of("73699180-bcf1-4bf3-8d04-676b8444b691")

        String guid = randomGuid();
        GuidTestGroupAssignmentStrategy strategy = new GuidTestGroupAssignmentStrategy();

        String toss = strategy.getAssignment(experiment, guid).getValue();
        String expandedToss = strategy.getAssignment(connectedExperiment, guid).getValue();

        if (toss.equals("NEW")) {
            assertThat(expandedToss, is("NEW"));
        }
    }


}

