package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import org.joda.time.DateTime;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.dateCreated;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author: talyag
 * @since: 12/9/13
 */
public class NewUsersFilterTest {

    @Test
    public void isNotEligibleForOldRegisteredUser() throws Exception {
        NewUsersFilter newUsersFilter = new NewUsersFilter();
        UserInfo oldUserInfo = a(UserInfoMakers.UserInfo).but(with(dateCreated, new DateTime().minusHours(1))).make();
        assertThat(newUsersFilter.isEligible(
                new EligibilityCriteria(oldUserInfo, new AdditionalEligibilityCriteria(), new DateTime())), is(false));
    }

    @Test
    public void isEligibleForNewRegisteredUser() throws Exception {
        NewUsersFilter newUsersFilter = new NewUsersFilter();
        UserInfo newUserInfo = a(UserInfoMakers.UserInfo).make();
        assertThat(newUsersFilter.isEligible(
                new EligibilityCriteria(newUserInfo, new AdditionalEligibilityCriteria(), new DateTime().minusHours(1))), is(true));
    }
}
