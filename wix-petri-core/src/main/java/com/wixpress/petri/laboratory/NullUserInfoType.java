package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;
import scala.Option;

import java.util.UUID;

public class NullUserInfoType implements UserInfoType {

    public NullUserInfoType() {
    }

    public boolean isAnonymous() {
        return true;
    }

    public TestGroup drawTestGroup(Experiment exp) {
        throw new UnsupportedOperationException("cannot conduct experiments when user info is NullUserInfo - are you trying to use Petri from a non-http flow?");
    }


    @Override
    public Option<UUID> persistentKernel() {
        return scala.Option.apply(null); //Scala's None
    }

    @Override
    public boolean shouldPersist() {
        return false;
    }

}