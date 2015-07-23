package com.wixpress.petri.petri;

import java.util.List;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public interface OriginalIDAwarePetriDao<T, V> extends HistoryAwarePetriDao<T, V> {

    List<T> fetchAllExperimentsGroupedByOriginalId();
}
