package com.wixpress.petri.experiments.domain;

import org.joda.time.DateTime;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.wixpress.petri.laboratory.EligibilityCriteriaTypes.LanguageCriterion;
import static com.wixpress.petri.laboratory.EligibilityCriteriaTypes.UserCreationDateCriterion;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.UserInfo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EligibilityCriteriaTest {

    @Test
    public void usesUserCreationDateOverride() {
        DateTime now = new DateTime();
        AdditionalEligibilityCriteria withUserDate = new AdditionalEligibilityCriteria().withCriterion(new UserCreationDateCriterion(now));
        EligibilityCriteria eligibilityCriteria = new EligibilityCriteria(a(UserInfo).make(), withUserDate, null);
        assertThat(eligibilityCriteria.getUserCreationDate(), is(now));
    }

    @Test
    public void usesLanguageOverride() {
        AdditionalEligibilityCriteria withLanguage = new AdditionalEligibilityCriteria().withCriterion(new LanguageCriterion("yy"));
        EligibilityCriteria eligibilityCriteria = new EligibilityCriteria(a(UserInfo).make(), withLanguage, null);
        assertThat(eligibilityCriteria.getLanguage(), is("yy"));
    }


}