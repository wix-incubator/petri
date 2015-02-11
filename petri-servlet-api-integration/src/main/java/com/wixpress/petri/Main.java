package com.wixpress.petri;

import com.wixpress.petri.experiments.domain.FilterTypeIdResolver;
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
            PropertiesConfiguration config = new PropertiesConfiguration("petri.properties");

            FilterTypeIdResolver.useDynamicFilterClassLoading();

            JsonRPCServer rpcServer = new PetriServerFactory(port(config), dbConfig(config)).makePetriServer();

            rpcServer.start();

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static int port(PropertiesConfiguration config) {
        return config.getInt("server.port");
    }

    private static DBConfig dbConfig(PropertiesConfiguration config) {
        final String username =  config.getString("db.username");
        final String password = config.getString("db.password");
        final String url = config.getString("db.url");
        return makeDBConfig(username, password, url);
    }

}
