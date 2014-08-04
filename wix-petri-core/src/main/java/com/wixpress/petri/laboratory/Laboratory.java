package com.wixpress.petri.laboratory;

import com.wixpress.petri.petri.SpecDefinition;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 8/18/13
 * Time: 1:23 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Laboratory {
    String conductExperiment(Class<? extends SpecDefinition> experimentKey, String fallbackValue);

    <T> T conductExperiment(Class<? extends SpecDefinition> experimentKey, T fallbackValue, TestResultConverter<T> resultConverter);

    Map<String, String> conductAllInScope(String scope);

    <T> T conductExperiment(String key, T fallbackValue, TestResultConverter<T> resultConverter);
}