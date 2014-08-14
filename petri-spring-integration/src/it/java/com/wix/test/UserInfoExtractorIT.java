package com.wix.test;

import com.wix.jetty.TestJettyServer;
import com.wixpress.petri.laboratory.UserInfo;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static com.wix.jackson.ObjectMapperFactory.getObjectMapper;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;


/**
 * User: Dalias
 * Date: 8/7/14
 * Time: 6:45 PM
 */
public class UserInfoExtractorIT {

    ObjectMapper objectMapper = getObjectMapper();

    @Before
    public void before() throws Exception {
        new TestJettyServer();
    }

    @Test
    public void getUserInfo() throws Exception {

        HttpClient client  = HttpClientBuilder.create().build();
        HttpGet request  = new HttpGet("http://127.0.0.1:9002/hello");
        request.addHeader("Accept", "application/json");
        HttpResponse response = client.execute(request);
        assertThat(response.getStatusLine().getStatusCode(), is(HttpStatus.SC_OK));
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
        UserInfo userInfo = objectMapper.readValue(responseString, UserInfo.class);
        assertThat(userInfo.email, is("email"));

    }
}
