package com.wixpress.petri.petri;

/**
 * @author: talyag
 * @since: 9/15/13
 */
public interface DeleteEnablingPetriDao<T, V> extends PetriDao<T, V> {

    void delete(String key);
}
