package com.wixpress.common.petri.e2e;

import com.wix.hoopoe.koboshi.it.RemoteDataFetcherDriver;
import com.wixpress.petri.experiments.domain.ConductibleExperiments;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.petri.ConductExperimentSummary;
import com.wixpress.petri.petri.HostResolver;
import com.wixpress.petri.util.ConductExperimentSummaryMatcher;
import org.junit.Test;

import java.util.List;

import static com.wixpress.petri.test.TestBuilders.experimentWithFirstWinning;
import static java.lang.Thread.sleep;
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
    public void reportsASingleExperiment() throws Exception {
        addSpec("THE_KEY");
        Experiment experiment = fullPetriClient.insertExperiment(experimentWithFirstWinning("THE_KEY").build());
        remoteDataFetcherDriver.fetch(ConductibleExperiments.class);

        final String experimentResult = sampleAppRunner.conductExperiment("THE_KEY", "FALLBACK_VALUE");
        assertThat(experimentResult,  is("a"));

        waitForReporter();
        List<ConductExperimentSummary> experimentReport = fullPetriClient.getExperimentReport(experiment.getId());
        assertThat(experimentReport.size(), is(1));
        assertThat(experimentReport,  contains(ConductExperimentSummaryMatcher.hasSummary(HostResolver.getServerName(), experiment.getId(), "a", 1l)));
    }

    private void waitForReporter() throws InterruptedException {
        sleep(1000);
    }


}
