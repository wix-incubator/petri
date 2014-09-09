package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.EligibilityField;

import java.util.HashMap;
import java.util.Map;

/**
 * @author talyag
 * @since 9/3/14
 */
public class EligibilityFields {

    private Map<Class<? extends EligibilityField>, EligibilityField> fields;

    public EligibilityFields() {
        this.fields = new HashMap<>();
    }

    public <T extends EligibilityField> T getField(Class<T> fieldClass) {
        return (T) fields.get(fieldClass);
    }

    public <T extends EligibilityField> EligibilityFields withField(T data) {
        fields.put(data.getClass(), data);
        return this;
    }
}
