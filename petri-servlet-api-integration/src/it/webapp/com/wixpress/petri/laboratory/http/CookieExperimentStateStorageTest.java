package com.wixpress.petri.laboratory.http;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
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
