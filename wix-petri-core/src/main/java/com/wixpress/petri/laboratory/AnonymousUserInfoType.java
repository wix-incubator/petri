package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;

public class AnonymousUserInfoType implements UserInfoType {

    private final TestGroupSelector selector;

    public AnonymousUserInfoType(TestGroupSelector selector) {
        this.selector = selector;
    }

    public boolean isAnonymous() {
        return true;
    }

    public TestGroup drawTestGroup(Experiment exp) {
        return selector.forAnonymousUsers(exp);
    }

    public String getStorageKey() {
        return ANONYMOUS_LOG_STORAGE_KEY;
    }
}