package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.UserInfo;

/**
 * @author talyag
 * @since 9/7/14
 */
public class FilterTestUtils {

    public static FilterEligibility defaultFilterEligibilityForUser(UserInfo userInfo) {
        return new FilterEligibility(userInfo, new EligibilityFields(), null);
    }

}
