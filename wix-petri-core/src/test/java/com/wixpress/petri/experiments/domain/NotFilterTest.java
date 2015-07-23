package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.IOException;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.UserInfo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class NotFilterTest {

    public static EligibilityCriteria IRRELEVANT_FITLER_ELGIBILITY = new EligibilityCriteria(a(UserInfo).make(), new AdditionalEligibilityCriteria(), null);

    @Test
    public void shouldReturnFalseIfInternalFilterIsTrue() {
        Filter alwaysTrueFilter = new ConstantFilter(true);
        Filter notFilter = new NotFilter(alwaysTrueFilter);
        assertThat(notFilter.isEligible(IRRELEVANT_FITLER_ELGIBILITY), is(false));
    }

    @Test
    public void shouldReturnTrueIfInternalFilterIsFalse() {
        Filter alwaysFalseFilter = new ConstantFilter(false);
        Filter notFilter = new NotFilter(alwaysFalseFilter);
        assertThat(notFilter.isEligible(IRRELEVANT_FITLER_ELGIBILITY), is(true));
    }

    @Test
    public void canBeSerializedAndDeserialized() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        String json = objectMapper.writeValueAsString(new NotFilter(new WixEmployeesFilter()));
        assertThat(objectMapper.readValue(json, NotFilter.class), Matchers.is(new NotFilter(new WixEmployeesFilter())));
    }

}
