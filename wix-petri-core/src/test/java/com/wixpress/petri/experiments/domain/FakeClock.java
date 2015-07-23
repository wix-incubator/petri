package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.petri.Clock;
import org.joda.time.DateTime;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class FakeClock implements Clock {
    private DateTime now;

    public FakeClock(DateTime now) {
        this.now = now;
    }

    @Override
    public DateTime getCurrentDateTime() {
        return now;
    }
}
