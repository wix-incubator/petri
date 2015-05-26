package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;
import scala.Option;
import scala.Some;

import java.util.UUID;

public class RegisteredUserInfoType implements UserInfoType {

    private final UUID userId;

    public RegisteredUserInfoType(UUID userId) {
        this.userId = userId;
    }

    public boolean isAnonymous() {
        return false;
    }

    public TestGroup drawTestGroup(Experiment exp) {
        return new GuidTestGroupAssignmentStrategy().getAssignment(exp, userId.toString());
    }

    @Override
    public Option<UUID> persistentKernel() {
        return new Some(userId);
    }

    @Override
    public boolean shouldPersist(){
       return true;
    }

}