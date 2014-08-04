package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: uri
 * Date: 4/27/14
 * Time: 3:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class NotFilterTest {

    @Test
    public void shouldReturnFalseIfInternalFilterIsTrue() {
        Filter alwaysTrueFilter = new ConstantFilter(true);
        Filter notFilter = new NotFilter(alwaysTrueFilter);
        assertThat(notFilter.isEligible(null, null), is(false));
    }

    @Test
    public void shouldReturnTrueIfInternalFilterIsFalse() {
        Filter alwaysFalseFilter = new ConstantFilter(false);
        Filter notFilter = new NotFilter(alwaysFalseFilter);
        assertThat(notFilter.isEligible(null, null), is(true));
    }

    @Test
    public void canBeSerializedAndDeserialized() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        String json = objectMapper.writeValueAsString(new NotFilter(new WixEmployeesFilter()));
        assertThat(objectMapper.readValue(json, NotFilter.class), Matchers.is(new NotFilter(new WixEmployeesFilter())));
    }

}
