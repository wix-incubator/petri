package com.wixpress.petri.petri;

import java.util.List;

/**
 * @author: talyag
 * @since: 9/15/13
 */
public interface HistoryAwarePetriDao<T, V> extends PetriDao<T, V> {

    List<T> getHistoryById(int id);
}
