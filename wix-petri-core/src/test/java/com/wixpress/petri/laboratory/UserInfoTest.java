package com.wixpress.petri.laboratory;

import com.natpryce.makeiteasy.Maker;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import org.junit.Test;

import java.util.UUID;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.anonymousExperimentsLog;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class UserInfoTest {

    public static final UUID USER_ID = UUID.randomUUID();

    private final Maker<UserInfo> registeredUserInfoMaker = a(UserInfoMakers.UserInfo, with(UserInfoMakers.userId, USER_ID));



    @Test
    public void removesAnonymousExperiments() {

        UserInfo userInfoWithAnonExperiments = registeredUserInfoMaker.but(
                with(anonymousExperimentsLog, "3#4")).make();

        UserInfo userInfoAfterRemove = userInfoWithAnonExperiments.removeAnonymousExperimentsWhere(new ExperimentsLog.Predicate() {
            @Override
            public boolean matches(int experimentId) {
                return true;
            }
        });
        assertThat(userInfoAfterRemove.anonymousExperimentsLog, is(""));
        assertThat(userInfoAfterRemove.experimentsLog, is(""));
    }

}
