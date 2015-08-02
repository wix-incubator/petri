package com.wixpress.petri.petri;

import java.util.UUID;

/**
 * User: Dalias
 * Date: 3/22/15
 * Time: 12:00 PM
 */
public interface PetriDeveloperApi {

    UserState getFullUserState(UUID userGuid);

}
