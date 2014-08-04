package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: uri
 * Date: 4/23/14
 * Time: 5:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class FilterTypeIdResolverTest {

    @Test
    public void createsUnrecognizedFilterWhenResolvingUnrecognizedId() throws Exception {
        JavaType filterFromUnknown = new FilterTypeIdResolver().typeFromId("UNKNOWN");
        assertTrue(filterFromUnknown.hasRawClass(UnrecognizedFilter.class));
    }
}
