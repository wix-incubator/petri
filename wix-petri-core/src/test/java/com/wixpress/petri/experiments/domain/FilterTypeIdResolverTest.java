package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class FilterTypeIdResolverTest {

    @Test
    public void createsUnrecognizedFilterWhenResolvingUnrecognizedId() throws Exception {
        JavaType filterFromUnknown = new FilterTypeIdResolver().typeFromId("UNKNOWN");
        assertTrue(filterFromUnknown.hasRawClass(UnrecognizedFilter.class));
    }
}
