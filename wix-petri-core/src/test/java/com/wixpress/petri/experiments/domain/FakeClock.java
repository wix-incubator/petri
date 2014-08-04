package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.petri.Clock;
import org.joda.time.DateTime;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 7/7/14
 * Time: 6:16 PM
 * To change this template use File | Settings | File Templates.
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
