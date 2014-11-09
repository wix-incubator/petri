package com.wixpress.petri.experiments.domain;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 3/9/14
 * Time: 3:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class NoFilter implements Filter {
    @Override
    public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
        return true;
    }
}
