package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.UserInfo;

/**
 * @author talyag
 * @since 9/7/14
 */
public class FilterTestUtils {

    public static EligibilityCriteria defaultEligibilityCriteriaForUser(UserInfo userInfo) {
        return new EligibilityCriteria(userInfo, new AdditionalEligibilityCriteria(), null);
    }

}
