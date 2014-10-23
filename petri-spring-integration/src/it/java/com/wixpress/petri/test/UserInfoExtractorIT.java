package com.wixpress.petri.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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

import javax.servlet.http.Cookie;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static final String extractUserInfoUrl = "http://localhost:9002/extractUserInfo";
    private static final List<Pair<String,String>> NO_PROPERTIES = null;

    private final ObjectMapper objectMapper = makeObjectMapper();

    @BeforeClass
    public static void before() throws Exception {
        testAppRunner.start();
    }

    @AfterClass
    public static void after() throws Exception {
        testAppRunner.stop();
    }


    private PropertyList aPropertyList(){
        return new PropertyList();
    }

    private class PropertyList{

        private List<Pair<String,String>> properties = new ArrayList<Pair<String,String>>();

        public PropertyList withPair(String key, String value){
            properties.add(new ImmutablePair<String, String>(key,value));
            return this;
        }

        public List<Pair<String,String>> build(){
            return properties;
        }

    }

    private void assertWithDefaultValueForProperty(Map<String, Object> userInfo, String propertyName, Object expectedValue, Object expectedDefaultValue) throws IOException {
        assertThat(userInfo.toString(),userInfo.get(propertyName), CoreMatchers.<Object>is(expectedValue));

        Map<String, Object> defaultUserInfo = extractUserInfoWithProperties(NO_PROPERTIES);
        assertThat(defaultUserInfo.toString(),defaultUserInfo.get(propertyName), CoreMatchers.<Object>is(expectedDefaultValue));
    }

    private Map<String, Object> extractUserInfoWithProperties(List<Pair<String,String>> properties) throws IOException {

        URL url = new URL(extractUserInfoUrl);
        URLConnection conn = url.openConnection();

        if (properties != NO_PROPERTIES) {
            for (Pair<String, String> property : properties) {
                conn.setRequestProperty(property.getKey(), property.getValue());
            }
        }

        conn.connect();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        return objectMapper.readValue(reader.readLine(), HashMap.class);
    }

    private String userExperimentCookieStr(String userId, String value) {
        return "_wixAB3|" + userId + "=" + value;
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
        String experimentsLog = "1#2";
        Map<String, Object> userInfo =
                extractUserInfoWithProperties(aPropertyList().withPair("Cookie", ANONYMOUS_LOG_STORAGE_KEY + "=" + experimentsLog).build());

        assertThat(userInfo.toString(),userInfo.get("anonymousExperimentsLog"), CoreMatchers.<Object>is(experimentsLog));
    }


    @Test
    public void extractsEmptyExperimentLogsByDefault() throws Exception {
        Map<String, Object> userInfo = extractUserInfoWithProperties(NO_PROPERTIES);
        assertThat(userInfo.toString(),userInfo.get("anonymousExperimentsLog"), CoreMatchers.<Object>is(""));
        assertThat(userInfo.toString(),userInfo.get("experimentsLog"), CoreMatchers.<Object>is(""));
    }

    @Test
    public void extractsEmptyExperimentOverridesByDefault() throws Exception {
        Map<String, Object> userInfo = extractUserInfoWithProperties(NO_PROPERTIES);
        final scala.collection.immutable.Map experimentOverrides = (scala.collection.immutable.Map) userInfo.get("experimentOverrides");
        assertThat(userInfo.toString(), experimentOverrides, isMapOfSize(0));
    }

    @Test
    public void extractsIpFromUserInfo() throws Exception {
        String clientIp = "9.0.2.10";
        Map<String, Object> userInfo =
                extractUserInfoWithProperties(aPropertyList().withPair("X-Forwarded-For", clientIp).build());

        assertWithDefaultValueForProperty(userInfo, "ip", clientIp, "127.0.0.1");
    }

    @Test
    public void extractsLanguageFromUserInfo() throws Exception {
        String languageTag = "es-ES";
        Map<String, Object> userInfo =
                extractUserInfoWithProperties(aPropertyList().withPair("Accept-Language", languageTag).build());

        assertWithDefaultValueForProperty(userInfo, "language", "es", "en");
    }

    @Test
    public void extractsCountryFromUserInfo() throws Exception {
        String country = "AF";
        Map<String, Object> userInfo =
                extractUserInfoWithProperties(aPropertyList().withPair("GEOIP_COUNTRY_CODE", country).build());

        assertWithDefaultValueForProperty(userInfo, "country", country, "US");
    }

    @Test
    public void extractsUserIdFromUserInfo() throws Exception {
        String userId = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6";
        String labUserIdCookie = "laboratory_user_id="+userId;
        Map<String, Object> userInfo =
                extractUserInfoWithProperties(
                        aPropertyList().withPair("Cookie", labUserIdCookie).build());

        assertWithDefaultValueForProperty(userInfo, "userId", userId, null);
    }

    @Test
    public void extractsExperimentsLogFromUserInfo() throws Exception {
        String userId = "f81d4fae-7dec-11d0-a765-00a0c91e6bf6";
        String experimentsLog = "1#2";
        String labUserIdCookie = "laboratory_user_id="+userId;

        Map<String, Object> userInfo =
                extractUserInfoWithProperties(
                        aPropertyList().withPair("Cookie", userExperimentCookieStr(userId, experimentsLog) + "; " + labUserIdCookie).build());

        assertWithDefaultValueForProperty(userInfo, "experimentsLog", experimentsLog, "");
    }

    @Test
    public void extractsIsRecurringFromUserInfo() throws Exception {
        Map<String, Object> userInfo = extractUserInfoWithProperties(NO_PROPERTIES);
        assertThat(userInfo.toString(),userInfo.get("isRecurringUser"), CoreMatchers.<Object>is(false));
    }
}
