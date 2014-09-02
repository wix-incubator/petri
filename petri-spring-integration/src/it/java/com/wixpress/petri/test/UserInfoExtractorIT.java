package com.wixpress.petri.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.common.petri.testutils.ServerRunner;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.springframework.core.io.FileSystemResource;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import static com.wixpress.petri.experiments.jackson.ObjectMapperFactory.makeObjectMapper;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * User: Dalias
 * Date: 8/7/14
 * Time: 6:45 PM
 */

public class UserInfoExtractorIT {

    ObjectMapper objectMapper = makeObjectMapper();

    private static final ServerRunner testAppRunner = new ServerRunner(9002,UserInfoExtractorIT.class.getResource("/").getPath() + "../../src/it/webapp");

    @BeforeClass
    public static void before() throws Exception {
        testAppRunner.start();
    }

    @AfterClass
    public static void after() throws Exception {
        testAppRunner.stop();
    }

    @Test
    public void getUserInfo() throws Exception {

        String googleBotUserAgent = "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)";

        HttpClient client  = HttpClientBuilder.create().build();
        String uri = "http://127.0.0.1:9002/extractUserInfo";
        HttpGet request  = new HttpGet(uri);
        request.addHeader("user-agent", googleBotUserAgent);

        HttpResponse response = client.execute(request);

        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        Map<String,Object> userInfo = objectMapper.readValue(responseString, HashMap.class);

        assertThat(userInfo.get("host"), CoreMatchers.<Object>is(InetAddress.getLocalHost().getHostName()));
        assertThat(userInfo.get("userAgent"), CoreMatchers.<Object>is(googleBotUserAgent));
        assertThat(userInfo.get("isRobot"), CoreMatchers.<Object>is(true));
        assertThat(userInfo.get("url"), CoreMatchers.<Object>is(uri));
        assertThat(userInfo.get("anonymous"), CoreMatchers.<Object>is(true));


    }
}
