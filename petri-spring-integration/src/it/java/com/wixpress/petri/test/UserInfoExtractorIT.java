package com.wixpress.petri.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
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

    private static final SampleAppRunner testAppRunner = new SampleAppRunner(9002);
    private static final String ANONYMOUS_LOG_STORAGE_KEY = "_wixAB3";


    private final ObjectMapper objectMapper = makeObjectMapper();

    @BeforeClass
    public static void before() throws Exception {
        testAppRunner.start();
    }

    @AfterClass
    public static void after() throws Exception {
        testAppRunner.stop();
    }

    private Map<String, Object> extractUserInfoWithAnonymousCookie(String anonCookieValue) throws IOException {
        URL url = new URL("http://localhost:9002/extractUserInfo");
        URLConnection conn = url.openConnection();
        conn.setRequestProperty("Cookie", ANONYMOUS_LOG_STORAGE_KEY + "=" + anonCookieValue);
        conn.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        return objectMapper.readValue(reader.readLine(), HashMap.class);
    }

    private Map<String, Object> extractUserInfo() throws IOException {
        URL url = new URL("http://localhost:9002/extractUserInfo");
        URLConnection conn = url.openConnection();
        conn.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        return objectMapper.readValue(reader.readLine(), HashMap.class);
    }

    private Matcher<scala.collection.immutable.Map> isMapOfSize(final int size) {
        return new TypeSafeDiagnosingMatcher<scala.collection.immutable.Map>() {
            @Override
            protected boolean matchesSafely(scala.collection.immutable.Map o, Description description) {
                return o.size() == size;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Map of size " + size);
            }
        };
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


    @Test
    public void extractsExperimentsLogFromAnonymousCookie() throws Exception {
        Map<String, Object> userInfo = extractUserInfoWithAnonymousCookie("1#2");
        assertThat(userInfo.toString(),userInfo.get("anonymousExperimentsLog"), CoreMatchers.<Object>is("1#2"));
    }


    @Test
    public void extractsEmptyExperimentLogsByDefault() throws Exception {
        Map<String, Object> userInfo = extractUserInfo();
        assertThat(userInfo.toString(),userInfo.get("anonymousExperimentsLog"), CoreMatchers.<Object>is(""));
        assertThat(userInfo.toString(),userInfo.get("experimentsLog"), CoreMatchers.<Object>is(""));
    }

    @Test
    public void extractsEmptyExperimentOverridesByDefault() throws Exception {
        Map<String, Object> userInfo = extractUserInfo();
        final scala.collection.immutable.Map experimentOverrides = (scala.collection.immutable.Map) userInfo.get("experimentOverrides");
        assertThat(userInfo.toString(), experimentOverrides, isMapOfSize(0));
    }


}
