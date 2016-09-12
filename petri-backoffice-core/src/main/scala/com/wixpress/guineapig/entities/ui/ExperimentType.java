package com.wixpress.guineapig.entities.ui;

public enum ExperimentType {

    AB_TESTING("abTest"), FEATURE_TOGGLE("featureToggle");

    private String type;

    ExperimentType(String s) {
        type = s;
    }

    public String getType() {
        return type;
    }
}
