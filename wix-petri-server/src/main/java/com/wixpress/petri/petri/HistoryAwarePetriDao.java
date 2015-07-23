package com.wixpress.petri.petri;

import java.util.List;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public interface HistoryAwarePetriDao<T, V> extends PetriDao<T, V> {

    List<T> getHistoryById(int id);
}
