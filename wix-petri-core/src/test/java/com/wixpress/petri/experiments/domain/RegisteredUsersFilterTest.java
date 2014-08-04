package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.AnonymousUserInfo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author: talyag
 * @since: 12/9/13
 */
public class RegisteredUsersFilterTest {

    @Test
    public void testIsEligible() throws Exception {
        RegisteredUsersFilter registeredUsersFilter = new RegisteredUsersFilter();
        UserInfo registeredUserInfo = a(UserInfoMakers.UserInfo).make();
        UserInfo anonymousUserInfo = AnonymousUserInfo.make();
        assertThat(registeredUsersFilter.isEligible(anonymousUserInfo, null), is(false));
        assertThat(registeredUsersFilter.isEligible(registeredUserInfo, null), is(true));
    }
}
