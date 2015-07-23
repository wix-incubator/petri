package com.wixpress.petri;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class PetriConfigFile {
    private String username = "";
    private String password = "";
    private String url = "";
    private int port = 9090;
    private final File properties = new File("petri.properties");
    private int conductionLimitIntervalInMillis = 5000;

    PetriConfigFile() {
    }

    public static PetriConfigFile aPetriConfigFile() {
        return new PetriConfigFile();
    }

    public PetriConfigFile withUsername(String username) {
        this.username = username;
        return this;
    }

    public PetriConfigFile withPassword(String password) {
        this.password = password;
        return this;
    }

    public PetriConfigFile withUrl(String url) {
        this.url = url;
        return this;
    }

    public PetriConfigFile withPort(int port) {
        this.port = port;
        return this;
    }

    public void save() throws ConfigurationException, IOException {
        properties.createNewFile();
        PropertiesConfiguration config = new PropertiesConfiguration(properties);
        config.setProperty("db.username", username);
        config.setProperty("db.password", password);
        config.setProperty("db.url", url);
        config.setProperty("server.port", port);
        config.setProperty("server.conductionLimitIntervalInMillis", conductionLimitIntervalInMillis);
        config.save();
    }

    public boolean delete() {
        return properties.delete();
    }
}
