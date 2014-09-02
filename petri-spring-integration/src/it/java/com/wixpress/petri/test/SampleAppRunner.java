package com.wixpress.petri.test;

import com.wixpress.common.petri.testutils.ServerRunner;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 9/2/14
* Time: 11:55 AM
* To change this template use File | Settings | File Templates.
*/
public class SampleAppRunner {
    private final ServerRunner sampleAppServer;
    private final int port;

    public SampleAppRunner(int port) {
        this.port = port;
        sampleAppServer = new ServerRunner(port,ServerRunner.class.getResource("/").getPath() + "../../src/it/webapp");
    }

    public void start() throws Exception {
        sampleAppServer.start();
    }

    public void stop() throws Exception {
        sampleAppServer.stop();
    }

    public String conductExperiment(String key, String fallback) throws IOException {
        HttpClient client  = HttpClientBuilder.create().build();
        String uri = "http://127.0.0.1:" +
                port +
                "/conductExperiment?key=" +
                key +
                "&fallback=" +
                fallback;
        HttpGet request  = new HttpGet(uri);

        HttpResponse response = client.execute(request);

        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }
}
