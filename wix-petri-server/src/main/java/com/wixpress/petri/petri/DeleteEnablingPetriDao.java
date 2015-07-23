package com.wixpress.petri.petri;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public interface DeleteEnablingPetriDao<T, V> extends PetriDao<T, V> {

    void delete(String key);
}
