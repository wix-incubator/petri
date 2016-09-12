package com.wixpress.guineapig.entities.ui;

import com.google.common.base.Predicate;
import com.wixpress.petri.experiments.domain.Experiment;

import javax.annotation.Nullable;

public class AlwaysTrueIsEditablePredicate implements Predicate<Experiment> {
    @Override
    public boolean apply(@Nullable Experiment experiment) {
        return true;
    }
}
