package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;

import java.util.List;

/**
* Created with IntelliJ IDEA.
* User: sagyr
* Date: 7/16/14
* Time: 4:29 PM
* To change this template use File | Settings | File Templates.
*/
public class BlowingUpCachedExperiments extends CachedExperiments {

    public BlowingUpCachedExperiments() {
        super(null);
    }

    @Override
    public List<Experiment> findNonExpiredByKey(String key) {
        throw new CacheExploded();
    }

    @Override
    public Experiment findById(int experimentId) {
        throw new CacheExploded();
    }

    @Override
    public List<Experiment> findNonExpiredByScope(final String scope) {
        throw new CacheExploded();
    }

    public static class CacheExploded extends RuntimeException {}
}
