package com.wixpress.petri.laboratory.http;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 10/23/14
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class CookieExperimentStateStorageTest {

    @Test
    public void testStoreExperimentsLog() throws Exception {
        MockHttpServletResponse resposne = new MockHttpServletResponse();
        CookieExperimentStateStorage storage = new CookieExperimentStateStorage(resposne);
        storage.storeExperimentsLog("the_key", "the log");
        final Cookie cookie = resposne.getCookie("the_key");
        assertThat(cookie.getValue(), is("the log"));
        assertThat(cookie.getMaxAge(),is(CookieExperimentStateStorage.COOKIE_AGE));
        assertThat(cookie.getPath(),is("/"));

    }
}
