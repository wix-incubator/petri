package com.wixpress.guineapig.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wixpress.guineapig.entities.ui.ExperimentReport;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSnapshot;
import com.wixpress.petri.experiments.domain.ExperimentSpec;
import com.wixpress.petri.petri.SearchParameters;

import java.io.IOException;
import java.util.List;

public interface GuineapigExperimentMgmtService {
    Experiment getExperimentById(int experimentId);

    List<Experiment> getHistoryById(int experimentId);

    ExperimentSpec getSpecForExperiment(String experimentKey);

    List<Experiment> getAllExperiments() throws JsonProcessingException, ClassNotFoundException;

    List<Experiment> searchExperiments(SearchParameters parameters) throws JsonProcessingException, ClassNotFoundException;

    ExperimentReport getExperimentReport(int experimentId);

    boolean newExperiment(ExperimentSnapshot snapshot) throws IOException, IllegalArgumentException;

    boolean updateExperiment(Experiment experiment, String userName) throws IOException, IllegalArgumentException;

    boolean pauseExperiment(int experimentId, String comment, String userName);

    boolean resumeExperiment(int experimentId, String comment, String userName);

    boolean terminateExperiment(int experimentId, String comment, String userName);
}
