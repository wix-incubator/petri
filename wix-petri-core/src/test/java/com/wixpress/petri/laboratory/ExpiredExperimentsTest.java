package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.laboratory.dsl.TestGroupMakers;
import org.junit.Test;

import java.util.ArrayList;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author sagyr
 * @since 8/8/13
 */
public class ExpiredExperimentsTest {

    private static final int UNKNOWN_EXPERIMENT = 666;

    @Test
    public void matchesUnknownExperiments() {
        InMemoryExperimentsSource cache = new InMemoryExperimentsSource();
        CachedExperiments experiments = new CachedExperiments(cache);
        ExpiredExperiments ee = new ExpiredExperiments(experiments, true);

        cache.write(asList(a(Experiment, with(id, 777)).make()));
        assertTrue(ee.matches(UNKNOWN_EXPERIMENT));
    }

    @Test
    public void neverMatchesIfEmpty() {
        InMemoryExperimentsSource cache = new InMemoryExperimentsSource();
        CachedExperiments experiments = new CachedExperiments(cache);
        ExpiredExperiments ee = new ExpiredExperiments(experiments, true);

        cache.write(new ArrayList<Experiment>());
        assertFalse(ee.matches(UNKNOWN_EXPERIMENT));
    }

    @Test
    public void neverMatchesIfCacheIsStale() {
        InMemoryExperimentsSource cache = new InMemoryExperimentsSource();
        CachedExperiments experiments = new CachedExperiments(cache);
        ExpiredExperiments ee = new ExpiredExperiments(experiments, true);

        cache.write(asList(a(Experiment, with(id, 777)).make()));
        cache.setStale(true);
        assertFalse(ee.matches(UNKNOWN_EXPERIMENT));
    }

    @Test
    public void matchesExpiredFeatureToggles() {
        InMemoryExperimentsSource cache = new InMemoryExperimentsSource();
        CachedExperiments experiments = new CachedExperiments(cache);
        ExpiredExperiments ee = new ExpiredExperiments(experiments, true);

        cache.write(asList(a(Experiment,
                        with(id, 777),
                        with(featureToggle, true),
                        with(testGroups, TestGroupMakers.TEST_GROUPS_WITH_FIRST_ALWAYS_WINNING)
        ).make()));
        assertTrue(ee.matches(777));
    }

    @Test
    public void notMatchesExpiredFeatureTogglesWhenFtIsOff() {
        InMemoryExperimentsSource cache = new InMemoryExperimentsSource();
        CachedExperiments experiments = new CachedExperiments(cache);
        ExpiredExperiments ee = new ExpiredExperiments(experiments, false);

        cache.write(asList(a(Experiment,
                        with(id, 777),
                        with(featureToggle, true),
                        with(testGroups, TestGroupMakers.TEST_GROUPS_WITH_FIRST_ALWAYS_WINNING)
        ).make()));
        assertFalse(ee.matches(777));
    }
}
