package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.Experiment;
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
        Experiment experiment = an(Experiment).make();
        assertThat(wixEmployeesFilter.isEligible(nonWixUserInfo, experiment), is(false));
    }

    @Test
    public void isEligibleForWixEmployee() throws Exception {
        WixEmployeesFilter wixEmployeesFilter = new WixEmployeesFilter();
        UserInfo wixUserInfo = a(UserInfoMakers.UserInfo, with(email, "someone@wix.com")).make();
        Experiment experiment = an(Experiment).make();
        assertThat(wixEmployeesFilter.isEligible(wixUserInfo, experiment), is(true));
    }
}
