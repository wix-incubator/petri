package com.wixpress.petri.laboratory.http;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 10/23/14
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class CookieExperimentStateStorageTest {

    private static final String someCookieName = "the_key";
    private static final String someCookieValue = "the log";

    @Test
    public void testStoreExperimentsLog() throws Exception {
        Mockery context = new Mockery();
        final HttpServletResponse response = context.mock(HttpServletResponse.class);
        context.checking(new Expectations() {{
            oneOf(response).addCookie(with(aCookieWith(someCookieName, someCookieValue)));
        }});

        CookieExperimentStateStorage storage = new CookieExperimentStateStorage(response);
        storage.storeAnonymousExperimentsLog(someCookieName, someCookieValue);
    }

    public static class CookieMatcher extends TypeSafeMatcher<Cookie> {
        private final String cookieName;
        private final String cookieValue;

        public CookieMatcher(String cookieName, String cookieValue) {
            this.cookieName = cookieName;
            this.cookieValue = cookieValue;
        }

        public boolean matchesSafely(Cookie cookie) {
            return cookie.getName().equals(cookieName) &&
                    cookie.getValue().equals(cookieValue) &&
                    cookie.getPath().equals("/") &&
                    cookie.getMaxAge() == CookieExperimentStateStorage.COOKIE_AGE;
        }

        public void describeTo(Description description) {
            description.appendText("a cookie with name: ")
                    .appendValue(cookieName)
                    .appendText(", and value: ")
                    .appendValue(cookieValue);
        }
    }

    @Factory
    public static Matcher<Cookie> aCookieWith(String cookieName, String cookieValue) {
        return new CookieMatcher(cookieName, cookieValue);
    }
}

