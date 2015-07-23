package com.wixpress.petri.laboratory;

import com.wixpress.petri.experiments.domain.Experiment;

import java.util.List;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
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
