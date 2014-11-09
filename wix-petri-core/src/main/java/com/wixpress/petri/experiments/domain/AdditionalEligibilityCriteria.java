package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.EligibilityCriterion;

import java.util.HashMap;
import java.util.Map;

/**
 * @author talyag
 * @since 9/3/14
 */
public class AdditionalEligibilityCriteria {

    private Map<Class<? extends EligibilityCriterion>, EligibilityCriterion> criteria;

    public AdditionalEligibilityCriteria() {
        this.criteria = new HashMap<>();
    }

    public <T extends EligibilityCriterion> T getCriterion(Class<T> fieldClass) {
        return (T) criteria.get(fieldClass);
    }

    public <T extends EligibilityCriterion> AdditionalEligibilityCriteria withCriterion(T data) {
        criteria.put(data.getClass(), data);
        return this;
    }
}
