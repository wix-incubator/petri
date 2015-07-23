package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.experiments.domain.FilterTestUtils.defaultEligibilityCriteriaForUser;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.language;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class LanguageFilterTest {

    @Test
    public void testIsEligible() throws Exception {
        LanguageFilter languageFilter = new LanguageFilter(asList("en"));
        UserInfo userWithEnglish = a(UserInfoMakers.UserInfo, with(language, "en")).make();
        UserInfo userWithOtherLanguage = a(UserInfoMakers.UserInfo).make();
        assertThat(languageFilter.isEligible(defaultEligibilityCriteriaForUser(userWithEnglish)), is(true));
        assertThat(languageFilter.isEligible(defaultEligibilityCriteriaForUser(userWithOtherLanguage)), is(false));
    }
}
