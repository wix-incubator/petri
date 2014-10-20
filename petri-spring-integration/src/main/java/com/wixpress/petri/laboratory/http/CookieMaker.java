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

    public static Cookie makeAnonymousCookie(String anonymousExperimentsLog) {
        // TODO: add specific unit tests that drive storing both anonymous and user experiments logs.
        // TODO: check that cookie age is 6 months and path is "/"
        return new Cookie(ANONYMOUS_COOKIE_NAME, anonymousExperimentsLog);
    }
}
