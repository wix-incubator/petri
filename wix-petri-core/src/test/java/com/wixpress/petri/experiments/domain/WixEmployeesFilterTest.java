package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.experiments.domain.FilterTestUtils.defaultFilterEligibilityForUser;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.email;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author: talyag
 * @since: 12/11/13
 */
public class WixEmployeesFilterTest {

    @Test
    public void isNotEligibleForNonWixEmployee() throws Exception {
        WixEmployeesFilter wixEmployeesFilter = new WixEmployeesFilter();
        UserInfo nonWixUserInfo = a(UserInfoMakers.UserInfo).make();
        assertThat(wixEmployeesFilter.isEligible(defaultFilterEligibilityForUser(nonWixUserInfo)), is(false));
    }

    @Test
    public void isEligibleForWixEmployee() throws Exception {
        WixEmployeesFilter wixEmployeesFilter = new WixEmployeesFilter();
        UserInfo wixUserInfo = a(UserInfoMakers.UserInfo, with(email, "someone@wix.com")).make();
        assertThat(wixEmployeesFilter.isEligible(defaultFilterEligibilityForUser(wixUserInfo)), is(true));
    }
}
