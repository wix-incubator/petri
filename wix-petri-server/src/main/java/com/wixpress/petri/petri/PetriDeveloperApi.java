package com.wixpress.petri.petri;

import java.util.UUID;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public interface PetriDeveloperApi {

    UserState getFullUserState(UUID userGuid);

}
