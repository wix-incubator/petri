package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.experiments.domain.FilterTestUtils.defaultEligibilityCriteriaForUser;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.email;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class WixEmployeesFilterTest {

    @Test
    public void isNotEligibleForNonWixEmployee() throws Exception {
        WixEmployeesFilter wixEmployeesFilter = new WixEmployeesFilter();
        UserInfo nonWixUserInfo = a(UserInfoMakers.UserInfo).make();
        assertThat(wixEmployeesFilter.isEligible(defaultEligibilityCriteriaForUser(nonWixUserInfo)), is(false));
    }

    @Test
    public void isEligibleForWixEmployee() throws Exception {
        WixEmployeesFilter wixEmployeesFilter = new WixEmployeesFilter();
        UserInfo wixUserInfo = a(UserInfoMakers.UserInfo, with(email, "someone@wix.com")).make();
        assertThat(wixEmployeesFilter.isEligible(defaultEligibilityCriteriaForUser(wixUserInfo)), is(true));
    }
}
