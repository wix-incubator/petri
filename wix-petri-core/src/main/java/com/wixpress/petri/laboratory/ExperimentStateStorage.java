package com.wixpress.petri.laboratory;

import java.util.Map;
import java.util.UUID;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public interface ExperimentStateStorage {
    void storeAnonymousExperimentsLog(String key, String experimentsLog);

    void storeUserExperimentsLog(UUID userInSessionId, UUID userIdToPersistBy, String experimentsLog);

    void storeExperimentsOverrides(Map<String, String> overrides);
}
