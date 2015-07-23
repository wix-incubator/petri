package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.UserInfo;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class FilterTestUtils {

    public static EligibilityCriteria defaultEligibilityCriteriaForUser(UserInfo userInfo) {
        return new EligibilityCriteria(userInfo, new AdditionalEligibilityCriteria(), null);
    }

}
