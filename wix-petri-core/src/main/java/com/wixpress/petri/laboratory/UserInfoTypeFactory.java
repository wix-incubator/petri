package com.wixpress.petri.laboratory;

import java.util.UUID;


/**
 * @author sagyr
 * @since 11/19/13
 */
public class UserInfoTypeFactory {

    private static final AssignmentStrategyTestGroupSelector selector = new AssignmentStrategyTestGroupSelector();

    public static UserInfoType make(UUID uuid) {
        return (uuid == null) ? new AnonymousUserInfoType(selector) : new RegisteredUserInfoType(selector, uuid);
    }
}
