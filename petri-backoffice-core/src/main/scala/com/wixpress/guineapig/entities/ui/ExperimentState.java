package com.wixpress.guineapig.entities.ui;

public enum ExperimentState {

    UNKNOWN("unknown"), ACTIVE("active"), PAUSED("paused"), FUTURE("future"), ENDED("ended");

    private String state;

    ExperimentState(String s) {
        state = s;
    }

    public String getState() {
        return state;
    }
}
