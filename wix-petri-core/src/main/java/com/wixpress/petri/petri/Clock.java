package com.wixpress.petri.petri;

import org.joda.time.DateTime;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 7/1/14
 * Time: 2:34 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Clock {
    public DateTime getCurrentDateTime();
}
