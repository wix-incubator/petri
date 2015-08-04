package com.wixpress.petri.laboratory.http;

import com.wixpress.petri.laboratory.ExperimentStateStorage;
import com.wixpress.petri.laboratory.UserInfoType;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 10/23/14
* Time: 5:56 PM
* To change this template use File | Settings | File Templates.
*/
public class CookieExperimentStateStorage implements ExperimentStateStorage {
    private static final int SIX_MONTHS_IN_SECONDS = 6 * 30 * 24 * 60 * 60;
    public static final int COOKIE_AGE = SIX_MONTHS_IN_SECONDS;

    private final HttpServletResponse response;

    public CookieExperimentStateStorage(HttpServletResponse response) {
        this.response = response;
    }

    static public String cookieKeyFor(UUID userId) {
        return UserInfoType.ANONYMOUS_LOG_STORAGE_KEY + "|" + userId;
    }

    private void storeExperimentsLog(String key, String experimentsLog) {
        final Cookie cookie = new Cookie(key, experimentsLog);
        cookie.setMaxAge(COOKIE_AGE);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    @Override
    public void storeAnonymousExperimentsLog(String key, String experimentsLog) {
        storeExperimentsLog(key, experimentsLog);
    }

    @Override
    public void storeUserExperimentsLog(UUID userInSessionId, UUID userIdToPersistBy, String experimentsLog) {
        storeExperimentsLog(cookieKeyFor(userInSessionId), experimentsLog);
    }

    @Override
    public void storeExperimentsOverrides(Map<String, String> overrides) {
        // TODO: implement this!
    }
}
