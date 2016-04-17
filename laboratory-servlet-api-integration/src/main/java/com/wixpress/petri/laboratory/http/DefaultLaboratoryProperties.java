package com.wixpress.petri.laboratory.http;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * User: itayk
 * Date: 06/11/14
 */
public class DefaultLaboratoryProperties implements LaboratoryProperties {

    private static final String LABORATORY_CONFIG_NAME = "laboratoryConfig";
    private static final String PETRI_COOKIE_KEY = "petri.cookie.key";

    private Properties properties = new Properties();

    public DefaultLaboratoryProperties(ServletContext context) {
        String laboratoryConfig = context.getInitParameter(LABORATORY_CONFIG_NAME);
        InputStream input = context.getResourceAsStream(laboratoryConfig);
        loadFromStream(input);
    }

    private void loadFromStream(InputStream input) {
        // TODO - Unit test the !@#$ out of this
        try {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return getProperty(key, null);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public String getPetriCookieName(){
        return getProperty(PETRI_COOKIE_KEY);
    }
}
