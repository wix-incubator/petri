package com.wixpress.petri.laboratory;

import com.wix.hoopoe.koboshi.cache.ReadOnlyTimestampedLocalCache;
import com.wix.hoopoe.koboshi.cache.TimestampedData;
import com.wixpress.petri.ExperimentsAndState;
import com.wixpress.petri.experiments.domain.ConductibleExperiments;
import com.wixpress.petri.petri.Clock;
import org.joda.time.Duration;
import org.joda.time.Minutes;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 7/16/14
* Time: 4:15 PM
* To change this template use File | Settings | File Templates.
*/
public class TransientCacheExperimentSource implements CachedExperiments.ExperimentsSource {
    private static final int KOBOSHI_INTERVAL = 5;
    private static final int REMOTE_FETCHING_LATENCY_DURATION = 1;
    private static final Duration TIME_TO_STALE =
            Minutes.minutes(KOBOSHI_INTERVAL).plus(REMOTE_FETCHING_LATENCY_DURATION).toStandardDuration();

    private final ReadOnlyTimestampedLocalCache<ConductibleExperiments> cache;
    private final Clock clock;

    public TransientCacheExperimentSource(ReadOnlyTimestampedLocalCache<ConductibleExperiments> cache, Clock clock) {
        this.cache = cache;
        this.clock = clock;
    }

    @Override
    public ExperimentsAndState read() {
        TimestampedData<ConductibleExperiments> timestampedData = cache.readTimestamped();
        return new ExperimentsAndState(timestampedData.data().experiments(), isStale(timestampedData));
    }

    private boolean isStale(final TimestampedData<ConductibleExperiments> timestampedData) {
        return timestampedData.lastUpdate().plus(TIME_TO_STALE).isBefore(clock.getCurrentDateTime());
    }
}
