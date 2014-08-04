package com.wixpress.petri.laboratory;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 3/10/14
 * Time: 11:23 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ExperimentStateStorage {
    void storeExperimentsLog(String key, String experimentsLog);

    void storeExperimentsOverrides(Map<String, String> overrides);
}
