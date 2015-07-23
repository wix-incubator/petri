package com.wixpress.petri.laboratory;

import java.util.UUID;


/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class UserInfoTypeFactory {

    public static UserInfoType make(UUID uuid) {
        return (uuid == null) ? new AnonymousUserInfoType() : new RegisteredUserInfoType(uuid);
    }
}
