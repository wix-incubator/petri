package com.wixpress.petri;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.guineapig.embeddedjetty.WebUiServer;
import com.wixpress.petri.experiments.domain.FilterTypeIdResolver;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.petri.PetriRpcServer;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import static com.wixpress.petri.DBConfig.makeDBConfig;

public class Main {
    public static void main(String... args) {
        try {
            createPetriServer().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PropertiesConfiguration getPropertiesConfiguration() throws ConfigurationException {
        return new PropertiesConfiguration("petri.properties");
    }

    public static DBConfig dbConfig() throws ConfigurationException {
        PropertiesConfiguration config = getPropertiesConfiguration();
        final String username = config.getString("db.username");
        final String password = config.getString("db.password");
        final String url = config.getString("db.url");
        return makeDBConfig(username, password, url);
    }


    public static Main createPetriServer() throws ConfigurationException {
        config = getPropertiesConfiguration();
        port = port();
        objectMapper = ObjectMapperFactory.makeObjectMapper();
        petriServerFactory = new PetriServerFactory(port, dbConfig());
        rpcServer = petriServerFactory.makePetriServer(objectMapper);
        return new Main(config);
    }

    public Main(PropertiesConfiguration config) {
        this.config = config;
    }

    private static PropertiesConfiguration config;
    private static Integer port;
    public static PetriRpcServer rpcServer;
    private static ObjectMapper objectMapper;
    private static PetriServerFactory petriServerFactory;
    private WebUiServer webUiServer;

    public void start() {
        try {
            FilterTypeIdResolver.useDynamicFilterClassLoading();

            petriServerFactory.makeConductionKeeper(conductionLimitIntervalInMillis());

            webUiServer = WebUiServer.createServer(port);
            webUiServer.initServer();
            JsonRPCServer.addServlets(rpcServer, objectMapper, webUiServer.getSpringContext());
            webUiServer.startServer();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void stop() throws Exception {
        webUiServer.stop();
    }

    private static int port() {
        return config.getInt("server.port");
    }

    private int conductionLimitIntervalInMillis() {
        return config.getInt("server.conductionLimitIntervalInMillis", 150000);
    }

    public String getDatabaseUrl() {
        return config.getString("db.url");
    }
}