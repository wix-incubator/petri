package com.wixpress.common.petri.e2e;

import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ScopeDefinition;
import com.wixpress.petri.experiments.domain.TestGroup;
import com.wixpress.petri.petri.ConductExperimentSummary;
import com.wixpress.petri.petri.FullPetriClient;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.util.ConductExperimentSummaryMatcher;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.List;

import static com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.anExperimentSnapshot;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.aNewlyGeneratedExperimentSpec;
import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 8/21/14
 * Time: 12:10 PM
 * To change this template use File | Settings | File Templates.
 */


public class PetriReportsTest extends BaseTest {


    @Test
    public void conductingASimpleExperiment() throws Exception {
        FullPetriClient fullPetriClient = fullPetriClient();
        PetriClient petriClient = petriClient();

        petriClient.addSpecs(asList(
                aNewlyGeneratedExperimentSpec("NEW_KEY").
                    withTestGroups(asList("a", "b")).
                    withScopes(new ScopeDefinition("the scope", false)).
                build()));

        DateTime now = new DateTime();
        Experiment experiment = fullPetriClient.insertExperiment(
                anExperimentSnapshot().
                        withStartDate(now.minusMinutes(1)).
                        withEndDate(now.plusYears(1)).
                        withKey("NEW_KEY").
                        withGroups(asList(new TestGroup(0, 100, "a"), new TestGroup(1, 0, "b"))).
                        withOnlyForLoggedInUsers(false).
                        build());
        assertThat(petriClient.fetchActiveExperiments().size(), is(1));

        sampleAppRunner.conductExperiment("NEW_KEY", "FALLBACK_VALUE");

        sleep(20);
        List<ConductExperimentSummary> experimentReport = fullPetriClient.getExperimentReport(experiment.getId());
        assertThat(experimentReport.size(), is(1));
        assertThat(experimentReport,  contains(ConductExperimentSummaryMatcher.hasSummary(experiment.getId(), "a", 1l)));
    }


}
