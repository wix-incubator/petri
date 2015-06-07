package com.wixpress.petri.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.common.petri.testutils.ServerRunner;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 9/2/14
* Time: 11:55 AM
* To change this template use File | Settings | File Templates.
*/
public class SampleAppRunner {
    public static final String DEFAULT_PATH_TO_WEBAPP = ServerRunner.class.getResource("/").getPath() + "../../src/it/webapp";
    private final ServerRunner sampleAppServer;
    private final int port;
    private final HttpClient client;

    public SampleAppRunner(int port){
        this(port, DEFAULT_PATH_TO_WEBAPP);
    }

    public SampleAppRunner(int port, String pathToWebapp){
        this(port, pathToWebapp, 0, false);
    }

    public SampleAppRunner(int port, String pathToWebapp, int reporterInterval, boolean useServerSideState) {
        this.port = port;
        this.sampleAppServer = new ServerRunner(port, pathToWebapp);
        this.client = HttpClientBuilder.create().build();

        if (reporterInterval != 0) {
            addReportingIntervalToProperties(pathToWebapp, reporterInterval);
        }
        addServerSideToProperties(pathToWebapp, useServerSideState);
    }

    private void addServerSideToProperties(String pathToWebapp, boolean useServerSideState) {
        File properties = new File(pathToWebapp + "/WEB-INF/laboratory.properties");
        try {
            properties.createNewFile();
            PropertiesConfiguration config = new PropertiesConfiguration(properties);
            config.setProperty("petri.writeStateToServer", useServerSideState);
            config.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SampleAppRunner SampleAppRunnerWithServerSideStateOff(int port) {
        return new SampleAppRunner(port, DEFAULT_PATH_TO_WEBAPP, 0, false);
    }

    public static SampleAppRunner SampleAppRunnerWithServerSideStateOn(int port) {
        return new SampleAppRunner(port, DEFAULT_PATH_TO_WEBAPP, 0, true);
    }

    private void addReportingIntervalToProperties(String pathToWebapp, int reporterInterval) {
        File properties = new File(pathToWebapp + "/WEB-INF/laboratory.properties");
        try {
            properties.createNewFile();
            PropertiesConfiguration config = new PropertiesConfiguration(properties);
            config.setProperty("reporter.interval", reporterInterval);
            config.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() throws Exception {
        sampleAppServer.start();
    }

    public void stop() throws Exception {
        sampleAppServer.stop();
    }

    private HttpClient newClient(){
        return HttpClientBuilder.create().build();
    }

    public String conductExperiment(String key, String fallback) throws IOException {
        String uri = "http://localhost:" +
                port +
                "/conductExperiment?key=" +
                key +
                "&fallback=" +
                fallback;
        HttpGet request  = new HttpGet(uri);
        HttpResponse response = client.execute(request);
        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }

    public String conductExperimentByUserWithNoPreviousCookies(String key, String fallback, UUID uuid) throws IOException {
        return conductExperimentByUser(key, fallback, uuid, true);
    }

    public String conductExperimentByUser(String key, String fallback, UUID uuid) throws IOException {
        return conductExperimentByUser(key, fallback, uuid, false);
    }

    // TODO: Remove duplication
    private String conductExperimentByUser(String key, String fallback, UUID uuid, boolean freshClient) throws IOException {
        String uri = "http://localhost:" +
                port +
                "/conductExperiment?key=" +
                key +
                "&laboratory_user_id=" +
                uuid.toString() +
                "&fallback=" +
                fallback;
        HttpGet request  = new HttpGet(uri);
        HttpClient clientToUse = freshClient ? newClient() : client;
        HttpResponse response = clientToUse.execute(request);
        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }


    public String conductExperimentWithCustomContext(String key, String fallback) throws IOException {
        String uri = "http://localhost:" +
                port +
                "/conductExperimentWithCustomContext?key=" +
                key +
                "&fallback=" +
                fallback;
        HttpPost request  = new HttpPost(uri);
        request.setHeader("Content-type", "application/json");
        HashMap<String, String> customContextMap = new HashMap<String, String>() {{
            put("userType", "special");
        }};
        request.setEntity(new StringEntity(new ObjectMapper().writeValueAsString(customContextMap)));
        HttpResponse response = newClient().execute(request);
        return EntityUtils.toString(response.getEntity(), "UTF-8");
    }



}
