package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;

public class NullUserInfoType implements UserInfoType {

    public NullUserInfoType() {
    }

    public boolean isAnonymous() {
        return true;
    }

    public TestGroup drawTestGroup(Experiment exp) {
        throw new UnsupportedOperationException("cannot conduct experiments when user info is NullUserInfo - are you trying to use Petri from a non-http flow?");
    }

    public String getStorageKey() {
        return "";
    }

}