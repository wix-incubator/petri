package com.wixpress.petri.petri;

import com.google.common.collect.ImmutableList;
import com.natpryce.makeiteasy.Maker;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSpec;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.internal.ExpectationBuilder;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.dao.DuplicateKeyException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.*;
import static com.wixpress.petri.laboratory.dsl.TestGroupMakers.*;
import static com.wixpress.petri.petri.PetriRpcServer.*;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.aNewlyGeneratedExperimentSpec;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.anExperimentSpec;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;

/**
 * @author: talyag
 * @since: 9/15/13
 */
public class PetriRpcServerTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    private final static DateTime currentTime = new DateTime();

    private PetriRpcServer petriRpcServer;
    private OriginalIDAwarePetriDao experimentsDao;
    private DeleteEnablingPetriDao specsDao;
    private PetriNotifier mailService;
    private Clock clock;
    private MetricsReportsDao metricsReportsDao;


    private final static Maker<Experiment> experimentMaker = an(Experiment,
            with(testGroups, listOf(
                    a(TestGroup,
                            with(groupId, 5),
                            with(probability, 100)),
                    a(TestGroup,
                            with(probability, 0))
            ))
    );
    private final static Maker<Experiment> activeExperiment = experimentMaker.but(
            with(key, "ex2"),
            with(startDate, currentTime.minusHours(1)));

    private final static Maker<Experiment> futureExperiment = experimentMaker.but(
            with(startDate, currentTime.plusHours(1)));

    private final static Maker<Experiment> expiredExperiment = experimentMaker.but(
            with(startDate, currentTime.minusHours(2)),
            with(endDate, currentTime.minusHours(1)));

    private SpecDefinition.ExperimentSpecBuilder defaultExperimentSpec() {
        return aNewlyGeneratedExperimentSpec("f.q.n").withTestGroups(Arrays.asList("on", "off")).withOwner("talyag@wix.com");
    }

    private void assumingDaoContainsExperiments(final List<Experiment> result) {
        context.checking(new Expectations() {{
            allowing(experimentsDao).fetch();
            will(returnValue(result));
        }});
    }

    private void assumingDaoContainsSpecs(final List<ExperimentSpec> result) {
        context.checking(new Expectations() {{
            allowing(specsDao).fetch();
            will(returnValue(result));
        }});
    }

    private void assumingTimeSourceReturnsNow() {
        context.checking(new Expectations() {{
            allowing(clock).getCurrentDateTime();
            will(returnValue(currentTime));
        }});
    }

    private void assumingTimeSourceReturns(final DateTime time) {
        context.checking(new Expectations() {{
            allowing(clock).getCurrentDateTime();
            will(returnValue(time));
        }});
    }

    private Expectations specIsUpdated(final ExperimentSpec theUpdatedSpec, final DateTime updateTime) {
        return new Expectations() {{
            oneOf(specsDao).update(with(theUpdatedSpec), with(updateTime));
        }};
    }

    private Expectations specIsIgnored() {
        return new Expectations() {{
            never(specsDao).update(with(any(ExperimentSpec.class)), with(any(DateTime.class)));
            never(specsDao).add(with(any(ExperimentSpec.class)));
        }};
    }

    private Expectations specIsAdded(final ExperimentSpec theNewSpec) {
        return new Expectations() {{
            oneOf(specsDao).add(theNewSpec);
        }};
    }

    private Expectations specIsDeleted(final String key) {
        return new Expectations() {{
            oneOf(specsDao).delete(key);
        }};
    }

    private Expectations specIsNotDeleted(final String key) {
        return new Expectations() {{
            never(specsDao).delete(key);
        }};
    }

    private Expectations specIsIgnoredAndEmailIsSent(final ExperimentSpec theUpdatedSpec, final ExperimentSpec theOriginalSpec) {
        return new Expectations() {{
            never(specsDao).update(with(any(ExperimentSpec.class)), with(any(DateTime.class)));
            never(specsDao).add(with(any(ExperimentSpec.class)));
            oneOf(mailService).notify(
                    with(stringContainsInOrder(asList(theUpdatedSpec.getKey(), NON_TERMINATED_EXPERIMENTS_MSG))),
                    with(stringContainsInOrder(asList(theOriginalSpec.toString(), theUpdatedSpec.toString()))),
                    with(containsString(theUpdatedSpec.getOwner())));
        }};
    }

    private Expectations specIsUpdatedAndEmailIsSent(final ExperimentSpec theUpdatedSpec, final DateTime updateTime, final String originalOwner) {
        return new Expectations() {{
            oneOf(specsDao).update(with(theUpdatedSpec), with(updateTime));
            oneOf(mailService).notify(with(String.format(SPEC_OWNER_CHANGED_MSG, theUpdatedSpec.getKey(), theUpdatedSpec.getOwner())),
                    with(any(String.class)),
                    with(originalOwner));
        }};
    }

    private ExpectationBuilder emailIsSent(final ExperimentSpec failedSpec) {
        return new Expectations() {{
            oneOf(mailService).notify(with(String.format(SPEC_UPDATE_FAILED_MSG, failedSpec.getKey())),
                    with(any(String.class)),
                    with(failedSpec.getOwner()));
        }};
    }

    @Before
    public void setUp() throws Exception {
        experimentsDao = context.mock(OriginalIDAwarePetriDao.class);
        specsDao = context.mock(DeleteEnablingPetriDao.class);
        clock = context.mock(Clock.class);
        mailService = context.mock(PetriNotifier.class);
        metricsReportsDao = context.mock(MetricsReportsDao.class);
        petriRpcServer = new PetriRpcServer(experimentsDao, clock, specsDao, mailService, metricsReportsDao);

    }

    @Test
    public void activeReturnsNoneWhenAllTestsAreExpired() throws Exception {
        assumingDaoContainsExperiments(asList(expiredExperiment.make()));
        assumingTimeSourceReturnsNow();
        assertThat(petriRpcServer.fetchActiveExperiments(), is(empty()));
    }


    @Test
    public void activeReturnsNoneWhenAllTestsAreFuture() throws Exception {
        assumingDaoContainsExperiments(asList(futureExperiment.make()));
        assumingTimeSourceReturnsNow();
        assertThat(petriRpcServer.fetchActiveExperiments(), is(empty()));
    }

    @Test
    public void activeReturnsActiveExperiments() throws Exception {
        assumingDaoContainsExperiments(asList(futureExperiment.make(), activeExperiment.make()));
        assumingTimeSourceReturnsNow();
        assertThat(petriRpcServer.fetchActiveExperiments(), is(asList(activeExperiment.make())));
    }

    @Test
    public void fetchReturnsAll() throws Exception {
        assumingDaoContainsExperiments(asList(expiredExperiment.make(), futureExperiment.make(), activeExperiment.make()));
        assertThat(petriRpcServer.fetchAllExperiments(), is(asList(expiredExperiment.make(), futureExperiment.make(), activeExperiment.make())));
    }

    @Test
    public void fetchFiltersIllegalExperiments() {
        assumingDaoContainsExperiments(asList(activeExperiment.make(), null));
        assertThat(petriRpcServer.fetchAllExperiments(), is(asList(activeExperiment.make())));
    }

    @Test
    public void fetchActiveFiltersIllegalExperiments() {
        assumingDaoContainsExperiments(asList(activeExperiment.make(), null));
        assumingTimeSourceReturnsNow();
        assertThat(petriRpcServer.fetchActiveExperiments(), is(asList(activeExperiment.make())));
    }

    @Test
    public void notifiesByMailWhenUpdatingSpecToDifferentOwner() {
        final ExperimentSpec theOriginalSpec = defaultExperimentSpec().build();

        DateTime updateDate = new DateTime().plusDays(1);
        assumingTimeSourceReturns(updateDate);
        assumingDaoContainsExperiments(new ArrayList<Experiment>());
        assumingDaoContainsSpecs(asList(theOriginalSpec));

        final ExperimentSpec expectedUpdatedSpec = defaultExperimentSpec().withOwner("someone.else@wix.com").build().
                setCreationDate(theOriginalSpec.getCreationDate());

        context.checking(specIsUpdatedAndEmailIsSent(expectedUpdatedSpec, updateDate, theOriginalSpec.getOwner()));
        petriRpcServer.addSpecs(asList(expectedUpdatedSpec));
    }

    @Test
    public void fetchSpecsFiltersIllegalSpecs() {
        final ExperimentSpec someExperimentSpec = defaultExperimentSpec().build();
        final ExperimentSpec illegalSpec = null;
        assumingDaoContainsSpecs(asList(someExperimentSpec, illegalSpec));
        assertThat(petriRpcServer.fetchSpecs(), is(asList(someExperimentSpec)));
    }

    @Test
    public void addSpecToEmptyCollectionAddsItToDao() {
        assumingDaoContainsSpecs(new ArrayList<ExperimentSpec>());
        assumingDaoContainsExperiments(new ArrayList<Experiment>());
        final ExperimentSpec theNewSpec = anExperimentSpec("name", new DateTime()).build();
        context.checking(specIsAdded(theNewSpec));
        petriRpcServer.addSpecs(asList(theNewSpec));
    }

    @Test
    public void updatesSpecWhenAddingModifiedVersionOfExistingSpec() {
        assumingDaoContainsExperiments(new ArrayList<Experiment>());

        DateTime creationDate = new DateTime();
        final ExperimentSpec theOriginalSpec = anExperimentSpec("f.q.n", creationDate).withTestGroups(Arrays.asList("on", "off")).build();
        DateTime updateDate = new DateTime().plusDays(1);
        final ExperimentSpec secondScanOfTheOriginalSpec = anExperimentSpec("f.q.n", updateDate).withTestGroups(Arrays.asList("x", "y")).build();

        assumingDaoContainsSpecs(asList(theOriginalSpec));
        assumingTimeSourceReturns(updateDate);

        final ExperimentSpec updatedSpecWithOriginalCreationTime = secondScanOfTheOriginalSpec.setCreationDate(creationDate);
        context.checking(specIsUpdated(updatedSpecWithOriginalCreationTime, updateDate));
        petriRpcServer.addSpecs(asList(secondScanOfTheOriginalSpec));
    }

    @Test
    public void ignoresSpecWhenAddingSameSpecTwice() {
        DateTime creationDate = new DateTime();
        final ExperimentSpec theOriginalSpec = anExperimentSpec("f.q.n", creationDate).withTestGroups(Arrays.asList("on", "off")).build();

        DateTime updateDate = new DateTime().plusHours(1);
        final ExperimentSpec secondScanOfTheOriginalSpec = anExperimentSpec("f.q.n", updateDate).withTestGroups(Arrays.asList("on", "off")).build();

        assumingDaoContainsSpecs(asList(theOriginalSpec));
        context.checking(specIsIgnored());
        petriRpcServer.addSpecs(asList(secondScanOfTheOriginalSpec));
    }

    @Test
    public void ignoresUpdatedSpecWhenFutureExperimentsExist() {
        final ExperimentSpec theOriginalSpec = defaultExperimentSpec().build();
        final ExperimentSpec theUpdatedSpec = defaultExperimentSpec().withTestGroups(Arrays.asList("x", "y")).build();
        assumingDaoContainsSpecs(asList(theOriginalSpec));
        assumingDaoContainsExperiments(asList(futureExperiment.but(with(key, theOriginalSpec.getKey())).make()));

        context.checking(specIsIgnoredAndEmailIsSent(theUpdatedSpec, theOriginalSpec));
        petriRpcServer.addSpecs(asList(theUpdatedSpec));
    }

    @Test
    public void notifiesByMailWhenUpdatingSpecWithExistingActiveExperiments() {
        final ExperimentSpec theOriginalSpec = defaultExperimentSpec().build();
        final ExperimentSpec theUpdatedSpec = defaultExperimentSpec().withTestGroups(Arrays.asList("x", "y")).build();
        assumingDaoContainsSpecs(asList(theOriginalSpec));
        assumingDaoContainsExperiments(asList(activeExperiment.but(with(key, theOriginalSpec.getKey())).make()));

        context.checking(specIsIgnoredAndEmailIsSent(theUpdatedSpec, theOriginalSpec));
        petriRpcServer.addSpecs(asList(theUpdatedSpec));
    }

    @Test
    public void canUpdateSpecsWhenInactiveExperimentsExistsForIt() {
        final ExperimentSpec theOriginalSpec = defaultExperimentSpec().build();
        final ExperimentSpec theUpdatedSpec = defaultExperimentSpec().withTestGroups(Arrays.asList("x", "y")).build();
        assumingDaoContainsSpecs(asList(theOriginalSpec));
        assumingDaoContainsExperiments(asList(expiredExperiment.but(with(key, theOriginalSpec.getKey())).make()));

        final DateTime updateTime = currentTime.plusDays(1);
        assumingTimeSourceReturns(updateTime);

        final ExperimentSpec updatedSpecWithOriginalCreationTime = theUpdatedSpec.setCreationDate(theOriginalSpec.getCreationDate());
        context.checking(specIsUpdated(updatedSpecWithOriginalCreationTime, updateTime));
        petriRpcServer.addSpecs(asList(theUpdatedSpec));
    }

    @Test
    public void addsSpecsEvenWhenFirstFails() {
        assumingDaoContainsSpecs(new ArrayList<ExperimentSpec>());
        assumingDaoContainsExperiments(new ArrayList<Experiment>());
        final ExperimentSpec aSpec = anExperimentSpec("name", new DateTime()).build();
        final ExperimentSpec duplicateSpec = anExperimentSpec("name", new DateTime()).withOwner("talya").build();
        final ExperimentSpec anotherSpec = anExperimentSpec("name1", new DateTime()).build();

        context.checking(specIsAdded(aSpec));
        context.checking(new Expectations() {{
            allowing(specsDao).add(duplicateSpec);
            will(throwException(new DuplicateKeyException("bla")));
        }});
        context.checking(specIsAdded(anotherSpec));
        context.checking(emailIsSent(duplicateSpec));

        petriRpcServer.addSpecs(asList(aSpec, duplicateSpec, anotherSpec));
    }

    @Test
    public void deleteSpec() {
        final ExperimentSpec spec = anExperimentSpec("name", new DateTime()).build();
        assumingDaoContainsSpecs(asList(spec));
        assumingDaoContainsExperiments(new ArrayList<Experiment>());

        context.checking(specIsDeleted(spec.getKey()));
        petriRpcServer.deleteSpec(spec.getKey());
    }

    @Test
    public void deleteFailsWhenSpecHasExistingExperiments() {
        final ExperimentSpec spec = anExperimentSpec("name", new DateTime()).build();
        assumingDaoContainsSpecs(asList(spec));
        assumingDaoContainsExperiments(asList(activeExperiment.but(with(key, spec.getKey())).make()));

        context.checking(specIsNotDeleted(spec.getKey()));
        petriRpcServer.deleteSpec(spec.getKey());
    }

    @Test
    public void reportConductExperiment() {
        final List<ConductExperimentReport> reports = ImmutableList.of(new ConductExperimentReport("localhost", 1, "true", 7l));
        context.checking(new Expectations() {{
            oneOf(metricsReportsDao).addReports(reports);
        }});
        petriRpcServer.reportConductExperiment(reports);
    }

    @Test
    public void getExperimentReport() {
        final List<ConductExperimentSummary> reports = ImmutableList.of(new ConductExperimentSummary("localhost", 1, "true", 7l, 11l, new DateTime()));
        context.checking(new Expectations() {{
            allowing(metricsReportsDao).getReport(1);
            will(returnValue(reports));        }});
        assertThat(petriRpcServer.getExperimentReport(1), is(reports));
    }


}