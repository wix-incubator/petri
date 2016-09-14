package com.wixpress.petri;

import com.wixpress.guineapig.embeddedjetty.WebUiServer;
import com.wixpress.petri.experiments.domain.FilterTypeIdResolver;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import static com.wixpress.petri.DBConfig.makeDBConfig;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 9/7/14
 * Time: 12:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {
    public static void main(String... args) {
        try {
            createPetriServer().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static PropertiesConfiguration getPropertiesConfiguration() throws ConfigurationException {
        return new PropertiesConfiguration("petri.properties");
    }

    private PropertiesConfiguration config;

    public static Main createPetriServer() throws ConfigurationException {
        return new Main(getPropertiesConfiguration());
    }

    public Main(PropertiesConfiguration config) {
        this.config = config;
    }

    private JsonRPCServer rpcServer;
    private WebUiServer webUiServer;

    public void start() {
        try {
            FilterTypeIdResolver.useDynamicFilterClassLoading();

            PetriServerFactory petriServerFactory = new PetriServerFactory(port(), dbConfig());

            rpcServer = petriServerFactory.makePetriServer();
            rpcServer.start();

            petriServerFactory.makeConductionKeeper(conductionLimitIntervalInMillis());

            webUiServer = WebUiServer.apply(config.getInt("uiserver.port"));
            webUiServer.start();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void stop() throws Exception {
        rpcServer.stop();
        webUiServer.stop();
    }

    private int port() {
        return config.getInt("server.port");
    }

    private int conductionLimitIntervalInMillis() {
        return config.getInt("server.conductionLimitIntervalInMillis", 150000);
    }

    private DBConfig dbConfig() {
        final String username = config.getString("db.username");
        final String password = config.getString("db.password");
        final String url = getDatabaseUrl();
        return makeDBConfig(username, password, url);
    }

    public String getDatabaseUrl() {
        return config.getString("db.url");
    }
}