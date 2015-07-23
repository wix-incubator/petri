package com.wixpress.petri.petri;

import java.util.UUID;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public interface UserRequestPetriClient {
    String getUserState(UUID userId);

}
