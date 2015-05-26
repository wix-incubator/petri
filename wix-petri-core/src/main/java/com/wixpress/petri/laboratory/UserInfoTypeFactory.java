package com.wixpress.petri.laboratory;

import java.util.UUID;


/**
 * @author sagyr
 * @since 11/19/13
 */
public class UserInfoTypeFactory {

    public static UserInfoType make(UUID uuid) {
        return (uuid == null) ? new AnonymousUserInfoType() : new RegisteredUserInfoType(uuid);
    }
}
