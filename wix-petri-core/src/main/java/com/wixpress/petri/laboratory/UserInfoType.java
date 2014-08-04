package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.TestGroup;

public interface UserInfoType extends TestGroupDrawer {

    public static final String ANONYMOUS_LOG_STORAGE_KEY = "_wixAB3";

    public boolean isAnonymous();

    public TestGroup drawTestGroup(Experiment exp);

    // TODO: Get rid of this since we dont need it anymore. We're always considering both
    // anonymous and personal cookies
    public String getStorageKey();
}