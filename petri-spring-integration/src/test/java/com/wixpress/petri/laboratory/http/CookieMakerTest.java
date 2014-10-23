package com.wixpress.petri.laboratory.http;

import org.junit.Test;

import javax.servlet.http.Cookie;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 10/22/14
 * Time: 11:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class CookieMakerTest {

    @Test
    public void makesAnonymousCookies() throws Exception {
        Cookie cookie = CookieMaker.makeAnonymousCookie("1#2");
        assertThat(cookie.getValue(),is("1#2"));
        assertThat(cookie.getName(),is("_wixAB3"));
        assertThat(cookie.getMaxAge(),is(CookieMaker.COOKIE_AGE));
        assertThat(cookie.getPath(),is("/"));
    }

    @Test
    public void makesUserCookies() throws Exception {
        Cookie cookie = CookieMaker.makeUserCookie("1#3", "THE_USER_ID");
        assertThat(cookie.getValue(),is("1#3"));
        assertThat(cookie.getName(),is("_wixAB3|THE_USER_ID"));
        assertThat(cookie.getMaxAge(),is(CookieMaker.COOKIE_AGE));
        assertThat(cookie.getPath(),is("/"));
    }

}
