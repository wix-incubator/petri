package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.laboratory.BrowserVersion;
import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import org.junit.Test;

import java.io.IOException;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.experiments.domain.FilterTestUtils.defaultEligibilityCriteriaForUser;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.browserVersion;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author: dalias
 * @since: 9/9/14
 */
public class BrowserVersionFilterTest {

    UserInfo userWithIE8 = a(UserInfoMakers.UserInfo, with(browserVersion, new BrowserVersion("IE", 8))).make();
    UserInfo userWithIE9 = a(UserInfoMakers.UserInfo, with(browserVersion, new BrowserVersion("IE", 9))).make();
    UserInfo userWithIE10 = a(UserInfoMakers.UserInfo, with(browserVersion, new BrowserVersion("IE", 10))).make();
    UserInfo userWithChrome = a(UserInfoMakers.UserInfo, with(browserVersion, new BrowserVersion("Chrome", 37))).make();
    UserInfo userWithFireFox = a(UserInfoMakers.UserInfo, with(browserVersion, new BrowserVersion("FireFox", 5))).make();

    @Test
    public void testIsEligibleOnIncludeFilter() throws Exception {
        BrowserVersionFilter includeIE9AndAboveFilter = new BrowserVersionFilter(asList(new BrowserVersion("IE", 9)), false);

        assertUserIsNotEligible(includeIE9AndAboveFilter, userWithIE8);
        assertUserIsEligible(includeIE9AndAboveFilter, userWithIE9);
        assertUserIsEligible(includeIE9AndAboveFilter, userWithIE10);
        assertUserIsNotEligible(includeIE9AndAboveFilter, userWithChrome);
    }

    @Test
    public void testIsEligibleOnExcludeFilter() throws Exception {
        BrowserVersionFilter excludeIE9AndBelowFilter = new BrowserVersionFilter(asList(new BrowserVersion("IE", 9)), true);

        assertUserIsNotEligible(excludeIE9AndBelowFilter, userWithIE8);
        assertUserIsEligible(excludeIE9AndBelowFilter, userWithIE9);
        assertUserIsEligible(excludeIE9AndBelowFilter, userWithIE10);
        assertUserIsEligible(excludeIE9AndBelowFilter, userWithChrome);
    }

    @Test
    public void testsEligibleWhenBrowserVersionIsEmpty() throws Exception {
        BrowserVersionFilter includeIE9AndAboveFilter = new BrowserVersionFilter(asList(new BrowserVersion("IE", 9)), false);
        BrowserVersionFilter excludeIE9AndBelowFilter = new BrowserVersionFilter(asList(new BrowserVersion("IE", 9)), true);

        UserInfo userWithEmptyBrowserVersion = a(UserInfoMakers.UserInfo, with(browserVersion, new BrowserVersion("", 0))).make();

        assertUserIsNotEligible(includeIE9AndAboveFilter, userWithEmptyBrowserVersion);
        assertUserIsEligible(excludeIE9AndBelowFilter, userWithEmptyBrowserVersion);

    }


    @Test
    public void testIsEligibleOnIncludeFilterWithMultipleBrowsers() throws Exception {
        BrowserVersionFilter includeIE9AndAboveAndChrome25AndAboveFilter = new BrowserVersionFilter(asList(new BrowserVersion("IE", 9), new BrowserVersion("Chrome", 35)), false);

        assertUserIsNotEligible(includeIE9AndAboveAndChrome25AndAboveFilter, userWithIE8);
        assertUserIsEligible(includeIE9AndAboveAndChrome25AndAboveFilter, userWithIE9);
        assertUserIsEligible(includeIE9AndAboveAndChrome25AndAboveFilter, userWithIE10);
        assertUserIsEligible(includeIE9AndAboveAndChrome25AndAboveFilter, userWithChrome);
        assertUserIsNotEligible(includeIE9AndAboveAndChrome25AndAboveFilter, userWithFireFox);

    }

    @Test
    public void testIsEligibleOnExcludeFilterWithMultipleBrowsers() throws Exception {
        BrowserVersionFilter excludeIE9AndBelowAndChrome25AndBelowFilter = new BrowserVersionFilter(asList(new BrowserVersion("IE", 9), new BrowserVersion("Chrome", 35)), true);


        assertUserIsNotEligible(excludeIE9AndBelowAndChrome25AndBelowFilter, userWithIE8);
        assertUserIsEligible(excludeIE9AndBelowAndChrome25AndBelowFilter, userWithIE9);
        assertUserIsEligible(excludeIE9AndBelowAndChrome25AndBelowFilter, userWithIE10);
        assertUserIsEligible(excludeIE9AndBelowAndChrome25AndBelowFilter, userWithChrome);
        assertUserIsEligible(excludeIE9AndBelowAndChrome25AndBelowFilter, userWithFireFox);

    }

    @Test
    public void canBeSerializedAndDeserialized() throws IOException {
        BrowserVersionFilter filter = new BrowserVersionFilter(asList(new BrowserVersion("IE", 9), new BrowserVersion("Chrome", 35)), false);

        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        String serialized = objectMapper.writeValueAsString(filter);
        BrowserVersionFilter deserialized = objectMapper.readValue(serialized, BrowserVersionFilter.class);
        assertThat(deserialized, is(filter));
    }

    private void assertUserIsEligible(Filter filter, UserInfo user) {
        assertThat(filter.isEligible(defaultEligibilityCriteriaForUser(user)), is(true));
    }

    private void assertUserIsNotEligible(Filter filter, UserInfo user) {
        assertThat(filter.isEligible(defaultEligibilityCriteriaForUser(user)), is(false));
    }

}
