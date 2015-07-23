package com.wixpress.petri.petri;

import org.joda.time.DateTime;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class JodaTimeClock implements Clock {

    @Override
    public DateTime getCurrentDateTime() {
        return new DateTime();
    }
}
