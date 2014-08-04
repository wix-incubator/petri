package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;

import java.util.UUID;

public class RegisteredUserInfoType implements UserInfoType {

    private final TestGroupSelector selector;
    private final UUID userId;

    public RegisteredUserInfoType(TestGroupSelector selector, UUID userId) {
        this.selector = selector;
        this.userId = userId;
    }

    public boolean isAnonymous() {
        return false;
    }

    public TestGroup drawTestGroup(Experiment exp) {

        return selector.forWixUser(exp, userId);
    }

    public String getStorageKey() {
        return ANONYMOUS_LOG_STORAGE_KEY + "|" + userId;
    }
}