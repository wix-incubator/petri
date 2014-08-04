package com.wixpress.petri.laboratory;

import com.wixpress.petri.petri.SpecDefinition;

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 3/31/14
 */
public interface PublicLaboratory extends Laboratory {
    String conductExperiment(Class<? extends SpecDefinition> experimentKey, String fallbackValue, ConductContext context);
    <T> T conductExperiment(Class<? extends SpecDefinition> experimentKey, T fallbackValue, TestResultConverter<T> resultConverter, ConductContext context);
    <T> T conductExperiment(String key, T fallbackValue, TestResultConverter<T> resultConverter, ConductContext context);
}
