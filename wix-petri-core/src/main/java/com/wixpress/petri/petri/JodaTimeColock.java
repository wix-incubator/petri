package com.wixpress.petri.petri;

import org.joda.time.DateTime;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 7/1/14
 * Time: 2:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class JodaTimeColock implements Clock {

    @Override
    public DateTime getCurrentDateTime() {
        return new DateTime();
    }
}
