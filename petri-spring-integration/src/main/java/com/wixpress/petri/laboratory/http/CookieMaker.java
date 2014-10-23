package com.wixpress.petri.laboratory.http;

import javax.servlet.http.Cookie;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 10/20/14
* Time: 6:03 PM
* To change this template use File | Settings | File Templates.
*/
public class CookieMaker {

    private static final String ANONYMOUS_COOKIE_NAME = "_wixAB3";
    private static final int SIX_MONTHS_IN_SECONDS = 6 * 30 * 24 * 60 * 60;
    public static final int COOKIE_AGE = SIX_MONTHS_IN_SECONDS;

    public static Cookie makeAnonymousCookie(String anonymousExperimentsLog) {
        return createPetriCookie(ANONYMOUS_COOKIE_NAME, anonymousExperimentsLog);
    }

    public static Cookie makeUserCookie(String usersExperimentsLog, String userId) {
        return createPetriCookie(ANONYMOUS_COOKIE_NAME + "|" + userId, usersExperimentsLog);
    }

    private static Cookie createPetriCookie(String name, String value) {
        final Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(COOKIE_AGE);
        cookie.setPath("/");
        return cookie;
    }

}
