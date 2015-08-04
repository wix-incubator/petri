package com.wixpress.petri.laboratory;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ExperimentOverridesUrlDecoder {

    public Map<String, String> decode(String petriExperimentOverridesUrlParam) {

        final String PAIR_DELIMITER = ":";
        final String EXPERIMENT_DELIMITER = ";";

        Map<String, String> experimentOverridesResult = new HashMap<String, String>();

        if (StringUtils.isBlank(petriExperimentOverridesUrlParam)) return experimentOverridesResult;

        final String[] experimentOverrides = StringUtils.split(petriExperimentOverridesUrlParam, EXPERIMENT_DELIMITER);
        if (experimentOverrides == null) return experimentOverridesResult;

        for (String experimentOverride : experimentOverrides) {
            final String[] experimentsOverrideResult = StringUtils.split(experimentOverride, PAIR_DELIMITER);
            if (experimentsOverrideResult.length == 2) {
                if (StringUtils.isNotBlank(experimentsOverrideResult[0]) && StringUtils.isNotBlank(experimentsOverrideResult[1])) {
                    experimentOverridesResult.put(experimentsOverrideResult[0], experimentsOverrideResult[1]);
                }
            }
        }

        return experimentOverridesResult;
    }
}