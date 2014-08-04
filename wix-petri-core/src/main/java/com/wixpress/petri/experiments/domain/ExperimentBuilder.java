package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.joda.time.DateTime;

/**
 * @author: talyag
 * @since: 12/4/13
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExperimentBuilder {

    private Integer id = Experiment.NO_ID;
    private DateTime lastUpdated = null;
    private ExperimentSnapshot experimentSnapshot = null;

    public static ExperimentBuilder anExperiment() {
        return new ExperimentBuilder();
    }

    public static ExperimentBuilder aCopyOf(Experiment experiment) {
        return anExperiment().
                withExperimentSnapshot(experiment.getExperimentSnapshot()).
                withId(experiment.getId()).
                withLastUpdated(experiment.getLastUpdated());
    }

    public ExperimentBuilder withId(Integer id) {
        this.id = id;
        return this;
    }

    public ExperimentBuilder withLastUpdated(DateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
        return this;
    }

    public ExperimentBuilder withExperimentSnapshot(ExperimentSnapshot experimentSnapshot) {
        this.experimentSnapshot = experimentSnapshot;
        return this;
    }

    public Experiment build() {
        if (lastUpdated == null) {
            throw new IllegalArgumentException("lastUpdated cannot be null");
        }
        if (experimentSnapshot == null) {
            throw new IllegalArgumentException("snapshot cannot be null");
        }
        if (id < 0) {
            throw new IllegalArgumentException("id cannot be negative");
        }

        return new Experiment(id, lastUpdated, experimentSnapshot);
    }

}
