package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;

import java.util.List;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public interface Experiments {
    List<Experiment> findNonExpiredByKey(String key);

    Experiment findById(int experimentId);

    List<Experiment> findNonExpiredByScope(String scope);

    boolean isUpToDate();

    boolean isEmpty();
}
