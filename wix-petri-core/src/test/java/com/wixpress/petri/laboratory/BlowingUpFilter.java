package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.Filter;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 7/16/14
* Time: 4:29 PM
* To change this template use File | Settings | File Templates.
*/
public class BlowingUpFilter implements Filter {
    @Override
    public boolean isEligible(UserInfo user, Experiment experiment) {
        throw new FilterExploded();
    }

    static public class FilterExploded extends RuntimeException {

    }
}
