package com.wixpress.guineapig.services;

import com.google.common.collect.ImmutableList;
import com.wixpress.guineapig.entities.ui.UiExperiment;
import com.wixpress.guineapig.entities.ui.UiExperimentBuilder;
import com.wixpress.guineapig.spi.FilterAdapterExtender;
import com.wixpress.petri.experiments.domain.Filter;

import java.util.List;

public class NoOpFilterAdapterExtender implements FilterAdapterExtender {
    @Override
    public void extendUiExperiment(List<Filter> filters, UiExperimentBuilder uiExperimentBuilder) {

    }

    @Override
    public Boolean isRecognized(Filter unrecognized)  {
        return false;
    }

    @Override
    public List<Filter> extractFiltersFromUiExperiment(UiExperiment uiExperiment) {
        return ImmutableList.of();
    }
}
