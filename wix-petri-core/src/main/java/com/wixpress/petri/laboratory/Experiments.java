package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 8/21/13
 * Time: 11:47 AM
 * To change this template use File | Settings | File Templates.
 */
public interface Experiments {
    List<Experiment> findNonExpiredByKey(String key);

    Experiment findById(int experimentId);

    List<Experiment> findNonExpiredByScope(String scope);

    boolean isUpToDate();

    boolean isEmpty();
}
