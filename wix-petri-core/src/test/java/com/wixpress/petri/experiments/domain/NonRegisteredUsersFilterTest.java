package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.experiments.domain.NonRegisteredUsersFilter;
import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.experiments.domain.FilterTestUtils.defaultEligibilityCriteriaForUser;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class NonRegisteredUsersFilterTest {

    @Test
    public void testIsEligible() throws Exception {
        NonRegisteredUsersFilter registeredUsersFilter = new NonRegisteredUsersFilter();
        UserInfo registeredUserInfo = a(UserInfoMakers.UserInfo).but(with(UserInfoMakers.registeredUserExists, true)).make();
        assertThat(registeredUsersFilter.isEligible(defaultEligibilityCriteriaForUser(registeredUserInfo)), is(false));

        UserInfo nonRegisteredUserInfo = a(UserInfoMakers.UserInfo).but(with(UserInfoMakers.registeredUserExists, false)).make();
        assertThat(registeredUsersFilter.isEligible(defaultEligibilityCriteriaForUser(nonRegisteredUserInfo)), is(true));
    }
}
