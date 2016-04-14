package com.wixpress.petri.laboratory.http;

/**
 * @author Laurent_Gaertner
 * @since 14-Apr-2016
 */
public interface PetriProperties {

    String getProperty(String key);

    String getProperty(String key, String defaultValue);

    String getPetriLogStorageCookieName();
}
