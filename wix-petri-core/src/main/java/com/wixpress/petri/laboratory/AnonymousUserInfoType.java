package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;
import scala.Option;

import java.util.UUID;

public class AnonymousUserInfoType implements UserInfoType {

    public AnonymousUserInfoType() {
    }

    public boolean isAnonymous() {
        return true;
    }

    public TestGroup drawTestGroup(Experiment exp) {
        return new AnonymousTestGroupAssignmentStrategy().getAssignment(exp, null);
    }


    @Override
    public Option<UUID> persistentKernel() {
        return scala.Option.apply(null); //Scala's None
    }

    @Override
    public boolean shouldPersist() {
        return true;
    }
}