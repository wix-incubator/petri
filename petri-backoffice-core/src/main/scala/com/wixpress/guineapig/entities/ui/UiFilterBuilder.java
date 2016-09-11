package com.wixpress.guineapig.entities.ui;

import java.util.ArrayList;
import java.util.List;

public class UiFilterBuilder {
    private String filterName = "";
    private boolean enabled = false;
    private List<String> mandatoryValue = new ArrayList<String>();
    private List<String> optionalValue = new ArrayList<String>();

    public UiFilterBuilder withFilterName(String filterName) {
        this.filterName = filterName;
        return this;
    }

    public UiFilterBuilder withEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public UiFilterBuilder withMandatoryValue(List<String> mandatoryValue) {
        this.mandatoryValue = mandatoryValue;
        return this;
    }

    public UiFilterBuilder withOptionalValue(List<String> optionalValue) {
        this.optionalValue = optionalValue;
        return this;
    }

    public UiFilter build() {
        return new UiFilter(filterName, enabled, mandatoryValue, optionalValue);
    }
}