package com.wixpress.petri.laboratory;

import com.wixpress.petri.petri.SpecDefinition;

import java.util.Map;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public interface Laboratory {
    String conductExperiment(Class<? extends SpecDefinition> experimentKey, String fallbackValue);

    String conductExperiment(Class<? extends SpecDefinition> experimentKey, String fallbackValue, ConductionContext context);

    <T> T conductExperiment(Class<? extends SpecDefinition> experimentKey, T fallbackValue, TestResultConverter<T> resultConverter);

    <T> T conductExperiment(Class<? extends SpecDefinition> experimentKey, T fallbackValue, TestResultConverter<T> resultConverter, ConductionContext context);

    Map<String, String> conductAllInScope(String scope);

    Map<String, String> conductAllInScope(String scope, ConductionContext context);

    <T> T conductExperiment(String key, T fallbackValue, TestResultConverter<T> resultConverter);

    <T> T conductExperiment(String key, T fallbackValue, TestResultConverter<T> resultConverter, ConductionContext context);
}