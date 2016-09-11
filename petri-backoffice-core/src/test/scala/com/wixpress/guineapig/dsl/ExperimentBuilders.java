package com.wixpress.guineapig.dsl;

import com.natpryce.makeiteasy.Maker;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import org.joda.time.DateTime;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.guineapig.services.ExperimentMgmtServiceTest.NOT_EDITABLE_SCOPE;

/**
 * @author: talyag
 * @since: 12/23/13
 */
public class ExperimentBuilders {

    public static Maker<Experiment> createActive() {
        return a(ExperimentMakers.Experiment,
                with(ExperimentMakers.startDate, new DateTime().minusHours(1)),
                with(ExperimentMakers.endDate, new DateTime().plusHours(1)));
    }
    public static Maker<Experiment> createActiveOnNonEditableScope() {
        return a(ExperimentMakers.Experiment,
                with(ExperimentMakers.startDate, new DateTime().minusHours(1)),
                with(ExperimentMakers.endDate, new DateTime().plusHours(1)),
                with(ExperimentMakers.scope, NOT_EDITABLE_SCOPE));
    }
    public static Maker<Experiment> createFuture() {
        return a(ExperimentMakers.Experiment,
                with(ExperimentMakers.startDate, new DateTime().plusHours(1)),
                with(ExperimentMakers.endDate, new DateTime().plusHours(2)));
    }
}
