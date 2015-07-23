package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "filter-type")
@JsonTypeIdResolver(FilterTypeIdResolver.class)
//!!! when creating new filter type add it in FilterTypeIdResolver !!!

public interface Filter {
    boolean isEligible(EligibilityCriteria eligibilityCriteria);

}
