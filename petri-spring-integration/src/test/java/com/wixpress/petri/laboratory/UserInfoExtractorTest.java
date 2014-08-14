package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.HostResolver;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.UUID;

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
    private UserInfoExtractor userInfoExtractor;

    @Before
    public void setup() {
        stubRequest = new MockHttpServletRequest();
        hostResolver =   mock(HostResolver.class);
        userInfoExtractor = new UserInfoExtractor(stubRequest, hostResolver);
    }


    @Test
    public void extractAUserInfoForIncomingRequest(){
        String host = "some host";
        when(hostResolver.resolve()).thenReturn(host);
        String userAgent = "Some-User-Agent-Bot";
        stubRequest.addHeader("user-agent",userAgent);

        UserInfo userInfo = userInfoExtractor.extract();

        assertThat(userInfo.host, is(host));
        assertThat(userInfo.userAgent, is(userAgent));
        assertThat(userInfo.isRobot, is(true));

    }

    @Test
    public void extractAUserInfoForNullRequest(){

        String host = "some host";
        when(hostResolver.resolve()).thenReturn(host);

        UserInfoExtractor userInfoExtractor = new UserInfoExtractor(null, hostResolver);

        UserInfo userInfo = userInfoExtractor.extract();

        UserInfo expectedUserInfo =   new UserInfo("", (UUID) null, null, "", "", "",
                new NullUserInfoType(), "", "", new DateTime(0), "", "", false, new HashMap<String, String>(), false, host);

        assertThat(userInfo, is(expectedUserInfo));

    }

    @Test
    public void extractAUserInfoForIncomingRequestWithNullHeaders(){
        String host = "some host";
        when(hostResolver.resolve()).thenReturn(host);

        UserInfo userInfo = userInfoExtractor.extract();

        assertThat(userInfo.host, is(host));
        assertThat(userInfo.userAgent, is(""));
        assertThat(userInfo.isRobot, is(false));

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

}
