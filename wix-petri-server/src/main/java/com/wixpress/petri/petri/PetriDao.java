package com.wixpress.petri.petri;


import org.joda.time.DateTime;

import java.util.List;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public interface PetriDao<T, V> {
    List<T> fetch();

    T add(V experimentSpec);

    void update(T experiment, DateTime currentDateTime);
}
