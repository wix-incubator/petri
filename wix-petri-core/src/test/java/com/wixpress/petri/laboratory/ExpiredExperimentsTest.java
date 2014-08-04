package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import org.junit.Test;

import java.util.ArrayList;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.Experiment;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.id;
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
        ExpiredExperiments ee = new ExpiredExperiments(experiments);

        cache.write(asList(a(Experiment, with(id, 777)).make()));
        assertTrue(ee.matches(UNKNOWN_EXPERIMENT));
    }

    @Test
    public void neverMatchesIfEmpty() {
        InMemoryExperimentsSource cache = new InMemoryExperimentsSource();
        CachedExperiments experiments = new CachedExperiments(cache);
        ExpiredExperiments ee = new ExpiredExperiments(experiments);

        cache.write(new ArrayList<Experiment>());
        assertFalse(ee.matches(UNKNOWN_EXPERIMENT));
    }

    @Test
    public void neverMatchesIfCacheIsNotUpToDate() {
        InMemoryExperimentsSource cache = new InMemoryExperimentsSource();
        CachedExperiments experiments = new CachedExperiments(cache);
        ExpiredExperiments ee = new ExpiredExperiments(experiments);

        cache.write(asList(a(Experiment, with(id, 777)).make()));
        cache.setIsUpToDate(false);
        assertFalse(ee.matches(UNKNOWN_EXPERIMENT));
    }
}
