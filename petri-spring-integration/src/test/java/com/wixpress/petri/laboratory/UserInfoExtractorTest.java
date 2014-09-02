package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.HostResolver;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: Dalias
 * Date: 8/14/14
 * Time: 3:45 PM
 */
public class UserInfoExtractorTest {

    private HostResolver hostResolver;
    private MockHttpServletRequest stubRequest;
    private HttpRequestUserInfoExtractor userInfoExtractor;


    private String host = "some host";
    private String appUrl = "http://server/app";
    private String userAgent = "Some-User-Agent-Bot";


    @Before
    public void setup() {
        stubRequest = new MockHttpServletRequest();
        hostResolver =   mock(HostResolver.class);
        userInfoExtractor = new HttpRequestUserInfoExtractor(stubRequest, hostResolver);
    }


    @Test
    public void extractAUserInfoForIncomingRequest(){
        when(hostResolver.resolve()).thenReturn(host);
        stubRequest.addHeader("user-agent", userAgent);
        stubRequest.addParameter("appUrl", appUrl);

        UserInfo userInfo = userInfoExtractor.extract();

        assertThat(userInfo.host, is(host));
        assertThat(userInfo.userAgent, is(userAgent));
        assertThat(userInfo.isRobot, is(true));
        assertThat(userInfo.url, is(appUrl));

    }

    @Test
    public void extractAUserInfoForNullRequest(){

        when(hostResolver.resolve()).thenReturn(host);

        UserInfoExtractor userInfoExtractor = new HttpRequestUserInfoExtractor(null, hostResolver);

        UserInfo userInfo = userInfoExtractor.extract();

        UserInfo expectedUserInfo =   new UserInfo("", null, null, "", "", "",
                new NullUserInfoType(), "", "", new DateTime(0), "", "", false, new HashMap<String, String>(), false, host);

        assertThat(userInfo, is(expectedUserInfo));

    }

    @Test
    public void extractAUserInfoForIncomingRequestWithNullHeaders(){
        when(hostResolver.resolve()).thenReturn(host);

        UserInfo userInfo = userInfoExtractor.extract();

        assertThat(userInfo.host, is(host));
        assertThat(userInfo.userAgent, is(""));
        assertThat(userInfo.isRobot, is(false));
        assertThat(userInfo.url, is("http://localhost"));

    }

    @Test
    public void checkIsRobot(){
        assertThat(userInfoExtractor.checkForRobotHeader("Bot-Agent"), is(true));
        assertThat(userInfoExtractor.checkForRobotHeader("crawler-Agent"), is(true));
        assertThat(userInfoExtractor.checkForRobotHeader("spider-Agent"), is(true));
        assertThat(userInfoExtractor.checkForRobotHeader("ping-Agent"), is(true));
        assertThat(userInfoExtractor.checkForRobotHeader("nagios-plugins-Agent"), is(true));
        assertThat(userInfoExtractor.checkForRobotHeader("My-Agent"), is(false));


    }

    @Test
    public void getUrlFromRequestAppUrlParamSet(){
        stubRequest.addParameter("appUrl", appUrl);
        assertThat(userInfoExtractor.getRequestURL(), is(appUrl));
    }

    @Test
    public void getUrlFromRequestServerNameNotLocalHost(){
        stubRequest.setServerName("test.wix.com");
        assertThat(userInfoExtractor.getRequestURL(), is("http://test.wix.com"));
    }

    @Test
    public void getUrlFromRequestServerNameIsLocalHostAndSmallXForwardHeader(){
        stubRequest.setServerName("localhost");
        stubRequest.addHeader("x-forwarded-host", "test.wix.com");
        assertThat(userInfoExtractor.getRequestURL(), is("http://test.wix.com"));
    }

    @Test
    public void getUrlFromRequestServerNameIsLocalHostAndCapitalXForwarsHeader(){
        stubRequest.setServerName("localhost");
        stubRequest.addHeader("X_FORWARDED_HOST", "test.wix.com");
        assertThat(userInfoExtractor.getRequestURL(), is("http://test.wix.com"));
    }

    @Test
    public void getUrlFromRequestServerNameIsLocalHostAndNoXForwarsHeader(){
        stubRequest.setServerName("localhost");
        assertThat(userInfoExtractor.getRequestURL(), is("http://localhost"));
    }

    @Test
    public void generatesAnonymousUserInfoByDefault() {
        assertThat(userInfoExtractor.extract().isAnonymous(), is(true));
    }

}
