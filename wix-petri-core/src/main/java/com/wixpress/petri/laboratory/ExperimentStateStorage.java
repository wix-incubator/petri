package com.wixpress.petri.laboratory;

import java.util.Map;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 3/10/14
 * Time: 11:23 AM
 * To change this template use File | Settings | File Templates.
 */
public interface ExperimentStateStorage {
    void storeAnonymousExperimentsLog(String experimentsLog);

    void storeUserExperimentsLog(UUID userInSessionId, UUID userIdToPersistBy, String experimentsLog);

    void storeExperimentsOverrides(Map<String, String> overrides);
}
