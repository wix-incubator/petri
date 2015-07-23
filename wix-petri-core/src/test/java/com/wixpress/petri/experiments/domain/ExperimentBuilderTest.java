package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.an;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class ExperimentBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void throwsWhenTryingToBuildWithNegativeId() {
        ExperimentBuilder.aCopyOf(an(ExperimentMakers.Experiment).make()).withId(-1).build();
    }
}
