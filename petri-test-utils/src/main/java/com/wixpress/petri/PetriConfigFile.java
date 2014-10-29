package com.wixpress.petri;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.io.File;
import java.io.IOException;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 9/9/14
* Time: 6:37 PM
* To change this template use File | Settings | File Templates.
*/
public class PetriConfigFile {
    private String username = "";
    private String password = "";
    private String url = "";
    private int port = 9090;
    private final File properties = new File("petri.properties");

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
        config.save();
    }

    public boolean delete() {
        return properties.delete();
    }
}
