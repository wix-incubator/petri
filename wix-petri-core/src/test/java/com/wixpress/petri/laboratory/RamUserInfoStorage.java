package com.wixpress.petri.laboratory;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class RamUserInfoStorage implements UserInfoStorage {
    private UserInfo info = null;

    @Override
    public void write(UserInfo info) {
        this.info = info;
    }

    @Override
    public UserInfo read() {
        return info;
    }

    public void assertUserExperimentsLog(String expected) {
        assertNotNull(info);
        assertThat(info.experimentsLog, is(expected));
    }

    public void assertAnonymousLogIs(String expected) {
        assertNotNull(info);
        assertThat(info.anonymousExperimentsLog, is(expected));
    }

    public void assertUserExperimentsLog(UUID otherUserGuid, String expected) {
        assertThat(info.otherUsersExperimentsLogs.get(otherUserGuid), is(expected));
    }

    public void assertUserExperimentsLogIsEmpty() {
        assertTrue(info.otherUsersExperimentsLogs.isEmpty());
    }
}
