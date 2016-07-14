package com.wixpress.petri.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.common.petri.testutils.ServerRunner;
import com.wixpress.petri.laboratory.FilterParametersExtractorsConfig;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

    private final Map<Path, Path> originalToTempConfigs = new HashMap<>();

    SampleAppRunner(int port){
        this(port, DEFAULT_PATH_TO_WEBAPP);
    }

    private SampleAppRunner(int port, String pathToWebapp){
        this(port, pathToWebapp, 0, false, Optional.empty());
    }

    static SampleAppRunner SampleAppRunnerWithServerSideStateOff(int port) {
        return new SampleAppRunner(port, DEFAULT_PATH_TO_WEBAPP, 0, false, Optional.empty());
    }

    static SampleAppRunner SampleAppRunnerWithServerSideStateOn(int port) {
        return new SampleAppRunner(port, DEFAULT_PATH_TO_WEBAPP, 0, true, Optional.empty());
    }

    public SampleAppRunner(int port, String pathToWebapp, int reporterInterval, boolean useServerSideState,
                           Optional<FilterParametersExtractorsConfig> filterParamsConfigOptional) {
        this.port = port;
        this.sampleAppServer = new ServerRunner(port, pathToWebapp);
        this.client = HttpClientBuilder.create().build();

        final File propertiesFile = setupConfigFilesBackup(pathToWebapp, filterParamsConfigOptional);

        if (reporterInterval != 0) {
            addReportingIntervalToProperties(propertiesFile, reporterInterval);
        }
        addServerSideToProperties(propertiesFile, useServerSideState);
    }

    private File setupConfigFilesBackup(String pathToWebapp, Optional<FilterParametersExtractorsConfig> filterParamsConfigOptional)  {
        try {
            final Path originalLabProps = Paths.get(pathToWebapp, "WEB-INF", "laboratory.properties");
            originalToTempConfigs.put(originalLabProps,
                    Files.createTempFile("laboratory-temp", "properties"));

            final Path originalFilterParamsConfig = Paths.get(pathToWebapp, "WEB-INF", "filters.yaml");
            optionallySetupBackupFiltersConfig(filterParamsConfigOptional, originalFilterParamsConfig);
            backupConfigs();
            optionallyOverrideFiltersConfig(filterParamsConfigOptional, originalFilterParamsConfig);
            return originalLabProps.toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void optionallyOverrideFiltersConfig(Optional<FilterParametersExtractorsConfig> filterParamsConfigOptional, Path originalFilterParamsConfig) {
        filterParamsConfigOptional.ifPresent(config -> new FilterParametersExtractorsConfigTestUtil()
                .replaceConfig(originalFilterParamsConfig.toFile(), config));
    }

    private void backupConfigs() {
        originalToTempConfigs.forEach((original, temp) -> {
            try {
                Files.copy(original, temp, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void optionallySetupBackupFiltersConfig(Optional<FilterParametersExtractorsConfig> filterParamsConfigOptional, Path originalFilterParamsConfig) {
        filterParamsConfigOptional
                .ifPresent(config -> {
                    try {
                        originalToTempConfigs.put(originalFilterParamsConfig,
                                Files.createTempFile("filters-temp", "yaml"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private void addServerSideToProperties(File propertiesFile, boolean useServerSideState) {
        setProperty(propertiesFile, useServerSideState, "petri.writeStateToServer");
    }

    private void addReportingIntervalToProperties(File propertiesFile, int reporterInterval) {
        setProperty(propertiesFile, reporterInterval, "reporter.interval");
    }

    private void setProperty(File propertiesFile, Object reporterInterval, String propertyName) {
        try {
            propertiesFile.createNewFile();
            PropertiesConfiguration config = new PropertiesConfiguration(propertiesFile);
            config.setProperty(propertyName, reporterInterval);
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
        revertChangesToConfigFiles();
    }

    private void revertChangesToConfigFiles() throws IOException {
        originalToTempConfigs.forEach((original, temp) -> {
            try {
                Files.copy(temp, original, StandardCopyOption.REPLACE_EXISTING);
                Files.deleteIfExists(temp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
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

    public String conductExperimentWithGeoHeader(String key, String fallback) throws IOException {
        String uri = "http://localhost:" +
                port +
                "/conductExperiment?key=" +
                key +
                "&fallback=" +
                fallback;
        HttpGet request  = new HttpGet(uri);
        request.setHeader("GEO_HEADER", "IL");
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
