package com.wixpress.petri;

import com.wixpress.petri.experiments.domain.FilterTypeIdResolver;
import com.wixpress.petri.petri.ConductionKeeper;
import com.wixpress.petri.petri.JodaTimeClock;
import org.apache.commons.configuration.PropertiesConfiguration;

import static com.wixpress.petri.DBConfig.makeDBConfig;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class Main {
    public static void main(String... args) {
        try {
            PropertiesConfiguration config = new PropertiesConfiguration("petri.properties");

            FilterTypeIdResolver.useDynamicFilterClassLoading();

            PetriServerFactory petriServerFactory = new PetriServerFactory(port(config), dbConfig(config));

            JsonRPCServer rpcServer = petriServerFactory.makePetriServer();
            rpcServer.start();

            petriServerFactory.makeConductionKeeper(conductionLimitIntervalInMillis(config));

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static int port(PropertiesConfiguration config) {
        return config.getInt("server.port");
    }

    private static int conductionLimitIntervalInMillis(PropertiesConfiguration config){
        return config.getInt("server.conductionLimitIntervalInMillis", 150000);
    }


    private static DBConfig dbConfig(PropertiesConfiguration config) {
        final String username =  config.getString("db.username");
        final String password = config.getString("db.password");
        final String url = config.getString("db.url");
        return makeDBConfig(username, password, url);
    }



}
