package com.wixpress.petri.petri;


import org.joda.time.DateTime;

import java.util.List;

/**
 * @author: talyag
 * @since: 9/30/13
 */
public interface PetriDao<T, V> {
    List<T> fetch();

    T add(V experimentSpec);

    void update(T experiment, DateTime currentDateTime);
}
