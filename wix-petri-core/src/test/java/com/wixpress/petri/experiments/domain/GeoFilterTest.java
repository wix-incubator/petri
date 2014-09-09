package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.experiments.domain.FilterTestUtils.defaultFilterEligibilityForUser;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.country;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author: talyag
 * @since: 12/9/13
 */
public class GeoFilterTest {

    @Test
    public void testIsEligible() throws Exception {
        GeoFilter geoFilter = new GeoFilter(asList("gb"));
        UserInfo userInGb = a(UserInfoMakers.UserInfo, with(country, "gb")).make();
        UserInfo userNotInGb = a(UserInfoMakers.UserInfo).make();
        assertThat(geoFilter.isEligible(defaultFilterEligibilityForUser(userInGb)), is(true));
        assertThat(geoFilter.isEligible(defaultFilterEligibilityForUser(userNotInGb)), is(false));
    }
}
