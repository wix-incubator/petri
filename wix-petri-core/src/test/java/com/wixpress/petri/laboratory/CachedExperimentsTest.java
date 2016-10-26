package com.wixpress.petri.laboratory;

import com.natpryce.makeiteasy.Maker;
import com.wix.hoopoe.koboshi.cache.TimestampedData;
import com.wix.hoopoe.koboshi.cache.transience.AtomicReferenceCache;
import com.wixpress.petri.experiments.domain.ConductibleExperiments;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.FakeClock;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.an;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.Experiment;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.scope;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author sagyr
 * @since 10/30/13
 */


public class CachedExperimentsTest {

    @Test
    public void findsAllExperimentsInScope() throws Exception {
        AtomicReferenceCache<ConductibleExperiments> cache = new AtomicReferenceCache<>();
        CachedExperiments cachedExperiments = new CachedExperiments(new TransientCacheExperimentSource(cache, new FakeClock(new DateTime())));
        Maker<Experiment> scopedExperiment = an(Experiment,
                with(scope, "scope1"));
        com.wixpress.petri.experiments.domain.Experiment experiment1 = scopedExperiment.make();
        com.wixpress.petri.experiments.domain.Experiment experiment2 = scopedExperiment.make();
        cache.write(new TimestampedData<>(new ConductibleExperiments(asList(experiment1, experiment2)), new Instant()));
        assertThat(cachedExperiments.findNonExpiredByScope("scope1"), is(asList(experiment1, experiment2)));
    }

}
