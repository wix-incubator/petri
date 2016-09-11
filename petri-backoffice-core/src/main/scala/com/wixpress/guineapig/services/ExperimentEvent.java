package com.wixpress.guineapig.services;


import com.wixpress.petri.experiments.domain.Experiment;

public class ExperimentEvent {

    public final static String CREATED = "created";
    public final static String TERMINATED = "terminated";
    public final static String UPDATED = "updated";
    public final static String RESUMED = "resumed";
    public final static String PAUSED = "paused";
    public final static String EXPANDED = "expanded";

    private final Experiment experiment;

    private final Experiment previousExperiment;
    private final String action;

    public Experiment getPreviousExperiment() {
        return previousExperiment;
    }

    public Experiment getExperiment() {
        return experiment;
    }

    public String getAction() {
        return action;
    }

    public ExperimentEvent(Experiment experiment, Experiment previousExperiment, String action) {
        this.experiment = experiment;
        this.action = action;
        this.previousExperiment = previousExperiment;
    }

}
