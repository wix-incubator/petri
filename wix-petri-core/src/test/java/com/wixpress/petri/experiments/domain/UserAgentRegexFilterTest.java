package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.experiments.domain.FilterTestUtils.defaultEligibilityCriteriaForUser;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.userAgent;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class UserAgentRegexFilterTest {

    UserInfo userWithAndroidUserAgent = a(UserInfoMakers.UserInfo, with(userAgent, "aa Android bb")).make();
    UserInfo userWithAndroidAndChromeUserAgent = a(UserInfoMakers.UserInfo, with(userAgent, "aa Android Chrome")).make();
    UserInfo userWithIphoneUserAgent = a(UserInfoMakers.UserInfo, with(userAgent, "aa Iphone bb")).make();
    String androidRegex = "(.*)Android(.*)";
    String chromeRegex = "(.*)Chrome(.*)";


    @Test
    public void testWhenIncludeAndExcludeAreDefinedOnFilter() throws Exception {
        UserAgentRegexFilter includeAndroidAndExcludeChromeRegexFilter = new UserAgentRegexFilter(asList(androidRegex), asList(chromeRegex));

        assertUserIsEligible(includeAndroidAndExcludeChromeRegexFilter, userWithAndroidUserAgent);
        assertUserIsNotEligible(includeAndroidAndExcludeChromeRegexFilter, userWithAndroidAndChromeUserAgent);
        assertUserIsNotEligible(includeAndroidAndExcludeChromeRegexFilter, userWithIphoneUserAgent);
    }

    @Test
    public void testWhenIncludeOnlyIsDefinedOnFilter() throws Exception {
        UserAgentRegexFilter includeAndroidRegexFilter = new UserAgentRegexFilter(asList(androidRegex), new ArrayList<String>());

        assertUserIsEligible(includeAndroidRegexFilter, userWithAndroidUserAgent);
        assertUserIsEligible(includeAndroidRegexFilter, userWithAndroidAndChromeUserAgent);
        assertUserIsNotEligible(includeAndroidRegexFilter, userWithIphoneUserAgent);
    }


    @Test
    public void testWhenExcludeOnlyIsDefinedOnFilter() throws Exception {
        UserAgentRegexFilter excludeChromeRegexFilter = new UserAgentRegexFilter(new ArrayList<String>(), asList(chromeRegex));

        assertUserIsEligible(excludeChromeRegexFilter, userWithAndroidUserAgent);
        assertUserIsNotEligible(excludeChromeRegexFilter, userWithAndroidAndChromeUserAgent);
        assertUserIsEligible(excludeChromeRegexFilter, userWithIphoneUserAgent);
    }

    @Test
    public void neverEligibleWhenNoRegexesAreDefinedOnFilter() throws Exception {
        UserAgentRegexFilter emptyFilter = new UserAgentRegexFilter(new ArrayList<String>(), new ArrayList<String>());

        assertUserIsNotEligible(emptyFilter, userWithAndroidUserAgent);
    }


    @Test
    public void canBeSerializedAndDeserialized() throws IOException {
        UserAgentRegexFilter filter = new UserAgentRegexFilter(asList(androidRegex), asList(chromeRegex));

        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        String serialized = objectMapper.writeValueAsString(filter);
        UserAgentRegexFilter deserialized = objectMapper.readValue(serialized, UserAgentRegexFilter.class);
        assertThat(deserialized, is(filter));
    }


    private void assertUserIsEligible(Filter filter, UserInfo user) {
        assertThat(filter.isEligible(defaultEligibilityCriteriaForUser(user)), is(true));
    }

    private void assertUserIsNotEligible(Filter filter, UserInfo user) {
        assertThat(filter.isEligible(defaultEligibilityCriteriaForUser(user)), is(false));
    }

}
