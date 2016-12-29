package com.wixpress.petri.laboratory;

import com.wixpress.petri.petri.HostResolver;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * User: Dalias
 * Date: 8/14/14
 * Time: 3:45 PM
 */
public class UserInfoExtractorTest {

    private MockHttpServletRequest stubRequest;
    private HttpRequestUserInfoExtractor userInfoExtractor;

    private static final String HOST = HostResolver.getServerName();
    private static final String APP_URL = "http://server/app";
    private static final String USER_AGENT = "Some-User-Agent-Bot";
    private static final String DEFAULT_SERVER_NAME = "localhost";
    private static final String DEFAULT_REQUEST_URL = "http://" + DEFAULT_SERVER_NAME;
    private static final String CUSTOM_SERVER_NAME = "test.wix.com";
    private static final String CUSTOM_REQUEST_URL = "http://" + CUSTOM_SERVER_NAME;
    private static final String PETRI_LOG_STORAGE_COOKIE_NAME = "cookieValue";

    @Before
    public void setup() {
        stubRequest = new MockHttpServletRequest();
        userInfoExtractor = new HttpRequestUserInfoExtractor(stubRequest, PETRI_LOG_STORAGE_COOKIE_NAME, FilterParametersConfig.apply());
    }

    @Test
    public void extractAUserInfoForIncomingRequest() {
        stubRequest.addHeader("user-agent", USER_AGENT);
        stubRequest.addParameter("appUrl", APP_URL);

        UserInfo userInfo = userInfoExtractor.extract();

        assertThat(userInfo.host, is(HOST));
        assertThat(userInfo.userAgent, is(USER_AGENT));
        assertThat(userInfo.isRobot, is(true));
        assertThat(userInfo.url, is(APP_URL));
    }

    @Test
    public void extractAUserInfoForNullRequest() {
        UserInfoExtractor userInfoExtractor = new HttpRequestUserInfoExtractor(null, PETRI_LOG_STORAGE_COOKIE_NAME, FilterParametersConfig.apply());

        UserInfo userInfo = userInfoExtractor.extract();
        UserInfo expectedUserInfo = new UserInfo("", null, null, "", "", "",
                new NullUserInfoType(), "", "", new DateTime(0), false, "", false, new HashMap<>(), false, HOST, false);

        assertThat(userInfo, is(expectedUserInfo));
    }

    @Test
    public void extractAUserInfoForIncomingRequestWithNullHeaders() {
        UserInfo userInfo = userInfoExtractor.extract();
        assertThat(userInfo.host, is(HOST));
        assertThat(userInfo.userAgent, is(""));
        assertThat(userInfo.isRobot, is(false));
        assertThat(userInfo.url, is(DEFAULT_REQUEST_URL));
    }

    @Test
    public void checkIsRobot() {
        assertThat(userInfoExtractor.checkForRobotHeader("Bot-Agent"), is(true));
        assertThat(userInfoExtractor.checkForRobotHeader("crawler-Agent"), is(true));
        assertThat(userInfoExtractor.checkForRobotHeader("spider-Agent"), is(true));
        assertThat(userInfoExtractor.checkForRobotHeader("ping-Agent"), is(true));
        assertThat(userInfoExtractor.checkForRobotHeader("nagios-plugins-Agent"), is(true));
        assertThat(userInfoExtractor.checkForRobotHeader("My-Agent"), is(false));
    }

    @Test
    public void getUrlFromRequestAppUrlParamSet() {
        stubRequest.addParameter("appUrl", APP_URL);
        assertThat(userInfoExtractor.getRequestURL(), is(APP_URL));
    }

    @Test
    public void getUrlFromRequestServerNameNotLocalHost() {
        stubRequest.setServerName(CUSTOM_SERVER_NAME);
        assertThat(userInfoExtractor.getRequestURL(), is(CUSTOM_REQUEST_URL));
    }

    @Test
    public void getUrlFromRequestServerNameIsLocalHostAndSmallXForwardHeader() {
        stubRequest.setServerName(DEFAULT_SERVER_NAME);
        stubRequest.addHeader("x-forwarded-host", CUSTOM_SERVER_NAME);
        assertThat(userInfoExtractor.getRequestURL(), is(CUSTOM_REQUEST_URL));
    }

    @Test
    public void getUrlFromRequestServerNameIsLocalHostAndCapitalXForwarsHeader() {
        stubRequest.setServerName(DEFAULT_SERVER_NAME);
        stubRequest.addHeader("X_FORWARDED_HOST", CUSTOM_SERVER_NAME);
        assertThat(userInfoExtractor.getRequestURL(), is(CUSTOM_REQUEST_URL));
    }

    @Test
    public void getUrlFromRequestServerNameIsLocalHostAndNoXForwarsHeader() {
        stubRequest.setServerName(DEFAULT_SERVER_NAME);
        assertThat(userInfoExtractor.getRequestURL(), is(DEFAULT_REQUEST_URL));
    }

    @Test
    public void generatesAnonymousUserInfoByDefault() {
        assertThat(userInfoExtractor.extract().isAnonymous(), is(true));
    }

    @Test
    public void languageResolverIsUsed() {
        final String someLang = "he";
        stubRequest.addParameter("Some_Param", someLang);
        UserInfoExtractor extractor = new HttpRequestUserInfoExtractor(stubRequest, PETRI_LOG_STORAGE_COOKIE_NAME,
                FilterParametersExtractorsConfigTestUtil.forParamOptionAndName(LanguageFilterParameter$.MODULE$,
                        ParamExtractionOption$.MODULE$, "Some_Param"));

        assertThat(extractor.extract().language, is(someLang));
    }

    @Test
    public void userIdResolverIsUsed() {
        final UUID someUser = UUID.randomUUID();
        stubRequest.addParameter("Some_Param", someUser.toString());
        UserInfoExtractor extractor = new HttpRequestUserInfoExtractor(stubRequest, PETRI_LOG_STORAGE_COOKIE_NAME,
                FilterParametersExtractorsConfigTestUtil.forParamOptionAndName(UserIdFilterParameter$.MODULE$,
                        ParamExtractionOption$.MODULE$, "Some_Param"));

        assertThat(extractor.extract().getUserId(), is(someUser));
    }

    @Test
    public void countryResolverIsUsed() {
        final String someCountry = "IL";
        stubRequest.addParameter("Some_Param", someCountry);
        UserInfoExtractor extractor = new HttpRequestUserInfoExtractor(stubRequest, PETRI_LOG_STORAGE_COOKIE_NAME,
                FilterParametersExtractorsConfigTestUtil.forParamOptionAndName(CountryFilterParameter$.MODULE$,
                        ParamExtractionOption$.MODULE$, "Some_Param"));

        assertThat(extractor.extract().country, is(someCountry));
    }
}
