package com.wixpress.petri.test;

import com.wixpress.petri.PetriRPCClient;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.petri.PetriClient;
import org.joda.time.DateTime;
import org.junit.Test;

import static com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.anExperimentSnapshot;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.aNewlyGeneratedExperimentSpec;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;


/**
 * User: Dalias
 * Date: 8/7/14
 * Time: 6:45 PM
 */


public class LaboratoryIT {

    public static final int SAMPLE_APP_PORT = 9015;
    public static final int PETRI_PORT = 9016;

    @Test
    public void conductingASimpleExperiment() throws Exception {

        final SampleAppRunner sampleApp = new SampleAppRunner(SAMPLE_APP_PORT);

        final FakePetriServer petri = new FakePetriServer(PETRI_PORT);

        petri.start();

        sampleApp.start();


        petri.addSpec(
                aNewlyGeneratedExperimentSpec("THE_KEY").
                        withTestGroups(asList("a", "b")).
                        withScopes(new ScopeDefinition("the scope", false)));

        DateTime now = new DateTime();

        petri.addExperiment(
                anExperimentSnapshot().
                        withStartDate(now.minusMinutes(1)).
                        withEndDate(now.plusYears(1)).
                        withKey("THE_KEY").
                        withGroups(asList(new TestGroup(0, 100, "a"), new TestGroup(1, 0, "b"))).
                        withOnlyForLoggedInUsers(false));


        PetriClient c = PetriRPCClient.makeFor("http://localhost:" +
                PETRI_PORT +
                "/");

        String testResult = sampleApp.conductExperiment("THE_KEY", "FALLBACK");
        assertThat(testResult, is("a"));

    }

}
