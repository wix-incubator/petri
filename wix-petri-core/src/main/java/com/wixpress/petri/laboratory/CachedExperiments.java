package com.wixpress.petri.laboratory;

import com.google.common.base.Predicate;
import com.wixpress.petri.experiments.domain.Experiment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.base.Predicates.and;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.newArrayList;
import static com.wixpress.petri.experiments.domain.ExperimentPredicates.HasID.hasID;
import static com.wixpress.petri.experiments.domain.ExperimentPredicates.HasKey.hasKey;
import static com.wixpress.petri.experiments.domain.ExperimentPredicates.IsInScope.isInScope;
import static com.wixpress.petri.experiments.domain.ExperimentPredicates.IsNotTerminated.isNotTerminated;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class CachedExperiments implements Experiments {

    private final ExperimentsSource source;

    public CachedExperiments(ExperimentsSource experimentSource) {
        this.source = experimentSource;
    }

    public static interface ExperimentsSource {
        public List<Experiment> read();

        public boolean isUpToDate();
    }

    @Override
    public List<Experiment> findNonExpiredByKey(String key) {
        return selectOrderedByFt(and(hasKey(key), isNotTerminated()));
    }

    @Override
    public Experiment findById(int experimentId) {
        return find(source.read(), hasID(experimentId), null);
    }

    @Override
    public List<Experiment> findNonExpiredByScope(final String scope) {
        return selectOrderedByFt(and(isInScope(scope), isNotTerminated()));
    }

    @Override
    public boolean isUpToDate() {
        return source.isUpToDate();
    }

    @Override
    public boolean isEmpty() {
        return source.read().isEmpty();
    }

    private List<Experiment> selectOrderedByFt(Predicate<Experiment> predicate) {
        ArrayList<Experiment> experiments = newArrayList(filter(this.source.read(), predicate));
        Collections.sort(experiments, new Comparator<Experiment>() {
            @Override
            public int compare(Experiment o1, Experiment o2) {
                if (o1.isToggle() && !o2.isToggle()) return -1;
                if (!o1.isToggle() && o2.isToggle()) return 1;
                return 0;
            }
        });
        return experiments;
    }


}
