package com.wixpress.petri.laboratory;


import com.google.common.collect.ImmutableMap;
import com.natpryce.makeiteasy.Maker;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import com.wixpress.petri.util.RepeatRule;
import org.junit.Rule;
import org.junit.Test;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.*;
import static com.wixpress.petri.laboratory.dsl.TestGroupMakers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * Created by talyag on 15/9/15.
 */
abstract public class AbstractTestGroupAssignmentStrategyTest {

    protected Maker<Experiment> exp5050(int aPercentage, int bPercentage) {
        return an(ExperimentMakers.Experiment,
                with(testGroups, listOf(
                        a(TestGroup,
                                with(probability, aPercentage),
                                with(value, "A")),
                        a(TestGroup,
                                with(probability, bPercentage),
                                with(value, "B"))
                )));
    }

    abstract TestGroupAssignmentStrategy strategy();

    protected Experiment experiment5050WithId(int expId, int aPercentage, int bPercentage){
        return exp5050(aPercentage, bPercentage).but(with(id, expId)).make();
    }

    protected Experiment experiment5050WithRandomId(int aPercentage, int bPercentage) {
        return exp5050(aPercentage, bPercentage).but(with(id, random())).make();
    }

    protected int random() {
        return (new Random()).nextInt(100000);
    }

    protected String randomGuid() {
        return UUID.randomUUID().toString();
    }

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    private void usersAreDistributedCorrectlyForASingleExperiment(int aPercentage, int bPercentage, int aDistributionMinRange, int aDistributionMaxRange, int bDistributionMinRange, int bDistributionMaxRange) {
        Map<String, AtomicInteger> assignments = ImmutableMap.of("A", new AtomicInteger(), "B", new AtomicInteger());

        Experiment experimentWithRandomId = experiment5050WithRandomId(aPercentage, bPercentage);
        for (int j = 0; j < 100000; j++) {
            String toss = strategy().getAssignment(experimentWithRandomId, randomGuid()).getValue();
            assignments.get(toss).incrementAndGet();
        }
        //99.99% confidence interval for 100,000 fair tosses at 50%
        assertThat("bad distribution for experiment id - " + experimentWithRandomId.getId(),
                assignments.get("A").get(), allOf(greaterThan(aDistributionMinRange), lessThan(aDistributionMaxRange)));
        assertThat(assignments.get("B").get(), allOf(greaterThan(bDistributionMinRange), lessThan(bDistributionMaxRange)));
    }

    private void experimentsAreDistributedIndependentlyForASpecificUser(int aPercentage, int bPercentage, int aDistributionMinRange, int aDistributionMaxRange, int bDistributionMinRange, int bDistributionMaxRange) {
        Map<String, AtomicInteger> assignments = ImmutableMap.of("A", new AtomicInteger(), "B", new AtomicInteger());

        String guid = randomGuid();
        for (int expID = 0; expID < 100000; expID++) {
            String toss = strategy().getAssignment(experiment5050WithId(expID, aPercentage, bPercentage), guid).getValue();
            assignments.get(toss).incrementAndGet();
        }
        //99.99% confidence interval for 100,000 fair tosses at 50%
        assertThat("bad distribution for user id - " + guid,
                assignments.get("A").get(), allOf(greaterThan(aDistributionMinRange), lessThan(aDistributionMaxRange)));
        assertThat(assignments.get("B").get(), allOf(greaterThan(bDistributionMinRange), lessThan(bDistributionMaxRange)));
    }

    // NOTE - these tests verify that the distribution really is uniform.
    // They may fail once in every 10000 runs (99.99% confidence level), and because of the repeat rule it could be once in 1000 runs on ci
    // if in any doubt, contact the nearest statistician

    @RepeatRule.Repeat(times = 10)
    @Test
    public void usersAreDistributedCorrectlyForASingleExperimentWithEvenGroups() {
        usersAreDistributedCorrectlyForASingleExperiment(50, 50, 49420, 50580, 49420, 50580);
    }

    @RepeatRule.Repeat(times = 10)
    @Test
    public void usersAreDistributedCorrectlyForASingleExperimentWithNonEvenGroups() {
        usersAreDistributedCorrectlyForASingleExperiment(20, 80, 19510, 20490, 79510, 80490);
    }

    @RepeatRule.Repeat(times = 10)
    @Test
    public void experimentsAreDistributedIndependentlyForASpecificUserWithEvenGroups() {
        experimentsAreDistributedIndependentlyForASpecificUser(50, 50, 49420, 50580, 49420, 50580);
    }

    @RepeatRule.Repeat(times = 10)
    @Test
    public void experimentsAreDistributedIndependentlyForASpecificUserWithNonEvenGroups() {
        experimentsAreDistributedIndependentlyForASpecificUser(20, 80, 19510, 20490, 79510, 80490);
    }




}

