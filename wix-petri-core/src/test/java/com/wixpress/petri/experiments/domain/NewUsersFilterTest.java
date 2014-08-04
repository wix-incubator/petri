package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import org.joda.time.DateTime;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.Experiment;
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
        Experiment experiment = an(Experiment).make();
        assertThat(newUsersFilter.isEligible(oldUserInfo, experiment), is(false));
    }

    @Test
    public void isEligibleForNewRegisteredUser() throws Exception {
        NewUsersFilter newUsersFilter = new NewUsersFilter();
        UserInfo newUserInfo = a(UserInfoMakers.UserInfo).make();
        Experiment experiment = an(Experiment, with(ExperimentMakers.startDate, new DateTime().minusHours(1))).make();
        assertThat(newUsersFilter.isEligible(newUserInfo, experiment), is(true));
    }
}
