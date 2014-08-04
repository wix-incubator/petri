package com.wixpress.petri.experiments.domain;

import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import org.junit.Test;

import static com.natpryce.makeiteasy.MakeItEasy.an;

/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 4/8/14
 * Time: 2:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExperimentBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void throwsWhenTryingToBuildWithNegativeId() {
        ExperimentBuilder.aCopyOf(an(ExperimentMakers.Experiment).make()).withId(-1).build();
    }
}
