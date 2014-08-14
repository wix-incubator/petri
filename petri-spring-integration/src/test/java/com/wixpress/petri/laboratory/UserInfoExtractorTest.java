package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.HostResolver;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

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

        UserInfo userInfo = userInfoExtractor.extract();

        assertThat(userInfo.host, is(host));

    }

}
