package com.wixpress.petri.petri;

import java.util.List;

/**
 * @author: talyag
 * @since: 9/15/13
 */
public interface OriginalIDAwarePetriDao<T, V> extends HistoryAwarePetriDao<T, V> {

    List<T> fetchAllExperimentsGroupedByOriginalId();
}
