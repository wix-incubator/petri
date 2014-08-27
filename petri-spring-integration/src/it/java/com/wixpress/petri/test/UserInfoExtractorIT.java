package com.wixpress.petri.test;

import com.wixpress.common.petri.testutils.ServerRunner;
import com.wixpress.petri.jetty.TestJettyServer;
import com.wixpress.petri.laboratory.UserInfo;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.*;

import java.net.InetAddress;

import static com.wixpress.petri.jackson.ObjectMapperFactory.getObjectMapper;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * User: Dalias
 * Date: 8/7/14
 * Time: 6:45 PM
 */
@Ignore
public class UserInfoExtractorIT {

    ObjectMapper objectMapper = getObjectMapper();
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
        //request.addHeader("Accept", "application/json");
        request.addHeader("user-agent", googleBotUserAgent);

//        Thread.sleep(5000);
        HttpResponse response = client.execute(request);

//        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        System.out.println(">>>>>>>>>>>>>>>>>" + responseString);
        UserInfo userInfo = objectMapper.readValue(responseString, UserInfo.class);

        assertThat(userInfo.host, is(InetAddress.getLocalHost().getHostName()));
        assertThat(userInfo.userAgent, is(googleBotUserAgent));
        assertThat(userInfo.isRobot, is(true));
        assertThat(userInfo.url, is(uri));

    }
}
