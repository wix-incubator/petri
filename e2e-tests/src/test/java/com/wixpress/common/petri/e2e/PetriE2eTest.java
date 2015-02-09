package com.wixpress.common.petri.e2e;

import com.wixpress.petri.Main;
import com.wixpress.petri.PetriRPCClient;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.petri.FullPetriClient;
import com.wixpress.petri.petri.PetriClient;
import com.wixpress.petri.test.SampleAppRunner;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import util.DBDriver;


import java.net.MalformedURLException;

import static com.wixpress.petri.PetriConfigFile.aPetriConfigFile;
import static com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.*;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.*;
import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * Created with IntelliJ IDEA.
 * User: sagyr
 * Date: 8/21/14
 * Time: 12:10 PM
 * To change this template use File | Settings | File Templates.
 */


public class PetriE2eTest  extends BaseTest {


    @Test
    public void conductingASimpleExperiment() throws Exception {
        FullPetriClient fullPetriClient = fullPetriClient();
        PetriClient petriClient = petriClient();

        petriClient.addSpecs(asList(
                aNewlyGeneratedExperimentSpec("THE_KEY").
                    withTestGroups(asList("a", "b")).
                    withScopes(new ScopeDefinition("the scope", false)).
                build()));

        DateTime now = new DateTime();
        fullPetriClient.insertExperiment(
                anExperimentSnapshot().
                        withStartDate(now.minusMinutes(1)).
                        withEndDate(now.plusYears(1)).
                        withKey("THE_KEY").
                    withGroups(asList(new TestGroup(0, 100, "a"), new TestGroup(1, 0, "b"))).
                    withOnlyForLoggedInUsers(false).
                build());
        assertThat(petriClient.fetchActiveExperiments().size(), is(1));

        String testResult = sampleAppRunner.conductExperiment("THE_KEY", "FALLBACK_VALUE");
        assertThat(testResult, is("a"));

    }


}
