package com.wixpress.petri.petri;

import java.util.UUID;

/**
 * User: Dalias
 * Date: 3/9/15
 * Time: 4:52 PM
 */
public interface UserRequestPetriClient {
    String getUserState(UUID userId);

}
