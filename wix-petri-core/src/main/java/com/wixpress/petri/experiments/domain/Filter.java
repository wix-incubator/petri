package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.wixpress.petri.laboratory.UserInfo;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 10/23/13
 * Time: 10:45 AM
 * To change this template use File | Settings | File Templates.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.PROPERTY, property = "filter-type")
@JsonTypeIdResolver(FilterTypeIdResolver.class)
//!!! when creating new filter type add it in FilterTypeIdResolver !!!

public interface Filter {
    boolean isEligible(UserInfo user, Experiment experiment);

}
