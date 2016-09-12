package com.wixpress.guineapig.spi;

import com.wixpress.guineapig.entities.ui.UiExperiment;
import com.wixpress.guineapig.entities.ui.UiExperimentBuilder;
import com.wixpress.petri.experiments.domain.Filter;

import java.util.List;

public interface FilterAdapterExtender {
    void extendUiExperiment(List<Filter> filters, UiExperimentBuilder uiExperimentBuilder);

    Boolean isRecognized(Filter unrecognized);

    List<Filter> extractFiltersFromUiExperiment(UiExperiment uiExperiment);
}
