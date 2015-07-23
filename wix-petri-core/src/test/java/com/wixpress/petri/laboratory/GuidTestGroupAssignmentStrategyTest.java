package com.wixpress.petri.laboratory;


import com.google.common.collect.ImmutableMap;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import com.wixpress.petri.util.RepeatRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.*;
import static com.wixpress.petri.laboratory.dsl.TestGroupMakers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/** 
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class GuidTestGroupAssignmentStrategyTest {

    public static final Experiment experiment = an(ExperimentMakers.Experiment,
            with(id, 1),
            with(key, "someKey"),
            with(scope, "someScope"),
            with(testGroups, listOf(
                    a(TestGroup,
                            with(probability, 80),
                            with(value, "OLD")),
                    a(TestGroup,
                            with(probability, 20),
                            with(value, "NEW"))
            ))
    ).make();
    public static final Experiment experimentWithOriginalId = an(ExperimentMakers.Experiment,
            with(id, 2),
            with(originalId, experiment.getId()),
            with(key, "someKey"),
            with(scope, "someScope"),
            with(testGroups, listOf(
                    a(TestGroup,
                            with(probability, 20),
                            with(value, "OLD")),
                    a(TestGroup,
                            with(probability, 80),
                            with(value, "NEW"))
            ))
    ).make();
    public static final Experiment linkedExperiment = an(ExperimentMakers.Experiment,
            with(id, 13),
            with(originalId, 11),
            with(linkedId, 1),
            with(key, "someKey"),
            with(scope, "someScope"),
            with(testGroups, listOf(
                    a(TestGroup,
                            with(probability, 20),
                            with(value, "OLD")),
                    a(TestGroup,
                            with(probability, 80),
                            with(value, "NEW"))
            ))
    ).make();

    private Experiment experimentWithRandomId() {
        return an(ExperimentMakers.Experiment,
                with(id, random()),
                with(key, "someKey"),
                with(scope, "someScope"),
                with(testGroups, listOf(
                        a(TestGroup,
                                with(probability, 80),
                                with(value, "OLD")),
                        a(TestGroup,
                                with(probability, 20),
                                with(value, "NEW"))
                ))
        ).make();
    }

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @RepeatRule.Repeat(times = 1000)
    @Test
    public void expandedExperimentCanOnlyReturnHigherTestGroup() throws Exception {
        // a 'magic number' - an example of a uid that causes the test to fail if original_id is ignored
        // (specifically for the testgroups with 20-80)
        // UserGuid.of("73699180-bcf1-4bf3-8d04-676b8444b691")

        String guid = randomGuid();
        GuidTestGroupAssignmentStrategy strategy = new GuidTestGroupAssignmentStrategy();

        String toss = strategy.getAssignment(experiment, guid).getValue();
        String expandedToss = strategy.getAssignment(experimentWithOriginalId, guid).getValue();

        if (toss.equals("NEW")) {
            assertThat(expandedToss, is("NEW"));
        }
    }

    @RepeatRule.Repeat(times = 1000)
    @Test
    public void linkedExperimentCanOnlyReturnHigherTestGroup() throws Exception {
        // a 'magic number' - an example of a uid that causes the test to fail if original_id is ignored
        // (specifically for the testgroups with 20-80)
        // UserGuid.of("73699180-bcf1-4bf3-8d04-676b8444b691")

        String guid = randomGuid();
        GuidTestGroupAssignmentStrategy strategy = new GuidTestGroupAssignmentStrategy();

        String toss = strategy.getAssignment(experiment, guid).getValue();
        String expandedToss = strategy.getAssignment(linkedExperiment, guid).getValue();

        if (toss.equals("NEW")) {
            assertThat(expandedToss, is("NEW"));
        }
    }

    @RepeatRule.Repeat(times = 100)
    @Test
    public void usersAreDistributedCorrectlyForASingleExperiment() {
        Map<String, AtomicInteger> assignments = ImmutableMap.of("OLD", new AtomicInteger(), "NEW", new AtomicInteger());
        GuidTestGroupAssignmentStrategy strategy = new GuidTestGroupAssignmentStrategy();

        for (int j = 0; j < 10000; j++) {
            String guid = randomGuid();
            String toss = strategy.getAssignment(experiment, guid).getValue();
            assignments.get(toss).incrementAndGet();
        }

        assertThat(assignments.get("NEW").get(), allOf(greaterThan(1800), lessThan(2200)));
        assertThat(assignments.get("OLD").get(), allOf(greaterThan(7800), lessThan(8200)));
    }

    @RepeatRule.Repeat(times = 100)
    @Test
    public void experimentsAreDistributesIndependentlyForASpecificUser() {
        Map<String, AtomicInteger> assignments = ImmutableMap.of("OLD", new AtomicInteger(), "NEW", new AtomicInteger());
        GuidTestGroupAssignmentStrategy strategy = new GuidTestGroupAssignmentStrategy();
        String guid = randomGuid();

        for (int j = 0; j < 10000; j++) {
            String toss = strategy.getAssignment(experimentWithRandomId(), guid).getValue();
            assignments.get(toss).incrementAndGet();
        }


        assertThat(assignments.get("NEW").get(), allOf(greaterThan(1500), lessThan(2500)));
        assertThat(assignments.get("OLD").get(), allOf(greaterThan(7500), lessThan(8500)));
    }

    private int random() {
        return (new Random()).nextInt(100000);
    }

    private String randomGuid() {
        return UUID.randomUUID().toString();
    }


}
