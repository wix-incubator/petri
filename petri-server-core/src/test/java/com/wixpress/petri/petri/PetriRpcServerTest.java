package com.wixpress.petri.petri;

import com.google.common.collect.ImmutableList;
import com.natpryce.makeiteasy.Maker;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSpec;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.internal.ExpectationBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.dao.DuplicateKeyException;

import java.util.*;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.*;
import static com.wixpress.petri.laboratory.dsl.TestGroupMakers.*;
import static com.wixpress.petri.petri.PetriRpcServer.*;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.aNewlyGeneratedExperimentSpec;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.anExperimentSpec;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
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
    private ExperimentsDao experimentsDao;
    private SpecsDao specsDao;
    private PetriNotifier mailService;
    private Clock clock;
    private MetricsReportsDao metricsReportsDao;
    private UserStateDao userStateDao;


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
            will(returnValue(scala.collection.JavaConverters.asScalaBufferConverter(result).asScala()));
        }});
    }

    private void assumingDaoContainsActiveExperiments(final List<Experiment> result) {
        context.checking(new Expectations() {{
            allowing(experimentsDao).fetchBetweenStartEndDates(with(any(DateTime.class)));
            will(returnValue(scala.collection.JavaConverters.asScalaBufferConverter(result).asScala()));
        }});
    }

    private void assumingDaoContainsSpecs(final List<ExperimentSpec> result) {
        context.checking(new Expectations() {{
            allowing(specsDao).fetch();
            will(returnValue(scala.collection.JavaConverters.asScalaBufferConverter(result).asScala()));
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
                    with(stringContainsInOrder(asList(theUpdatedSpec.getKey(), printNonTerminatedExperimentsMsg()))),
                    with(stringContainsInOrder(asList(theOriginalSpec.toString(), theUpdatedSpec.toString()))),
                    with(arrayContaining(containsString(theUpdatedSpec.getOwner()))));
        }};
    }

    private Expectations specIsUpdatedAndEmailIsSent(final ExperimentSpec theUpdatedSpec, final DateTime updateTime, final String originalOwner) {
        return new Expectations() {{
            oneOf(specsDao).update(with(theUpdatedSpec), with(updateTime));
            oneOf(mailService).notify(with(printSpecOwnerChangedMsg(theUpdatedSpec.getKey(), theUpdatedSpec.getOwner())),
                    with(any(String.class)),
                    with(arrayContaining(originalOwner)));
        }};
    }

    private ExpectationBuilder emailIsSent(final ExperimentSpec failedSpec) {
        return new Expectations() {{
            oneOf(mailService).notify(with(printSpecUpdateFailedMsg(failedSpec.getKey())),
                    with(any(String.class)),
                    with(arrayContaining(failedSpec.getOwner())));
        }};
    }

    @Before
    public void setUp() throws Exception {
        experimentsDao = context.mock(ExperimentsDao.class);
        specsDao = context.mock(SpecsDao.class);
        clock = context.mock(Clock.class);
        mailService = context.mock(PetriNotifier.class);
        metricsReportsDao = context.mock(MetricsReportsDao.class);
        userStateDao = context.mock(UserStateDao.class);
        petriRpcServer = new PetriRpcServer(experimentsDao, clock, specsDao, mailService, metricsReportsDao, userStateDao);

    }

    @Test
    public void activeReturnsNoneWhenAllTestsAreExpired() throws Exception {
        assumingDaoContainsActiveExperiments(asList(expiredExperiment.make()));
        assumingTimeSourceReturnsNow();
        assertThat(petriRpcServer.fetchActiveExperiments(), is(empty()));
    }


    @Test
    public void activeReturnsNoneWhenAllTestsAreFuture() throws Exception {
        assumingDaoContainsActiveExperiments(asList(futureExperiment.make()));
        assumingTimeSourceReturnsNow();
        assertThat(petriRpcServer.fetchActiveExperiments(), is(empty()));
    }

    @Test
    public void activeReturnsActiveExperiments() throws Exception {
        assumingDaoContainsActiveExperiments(asList(futureExperiment.make(), activeExperiment.make()));
        assumingTimeSourceReturnsNow();
        assertThat(petriRpcServer.fetchActiveExperiments(), is(asList(activeExperiment.make())));
    }

    @Test
    public void fetchActiveFiltersIllegalExperiments() {
        assumingDaoContainsActiveExperiments(asList(activeExperiment.make(), null));
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


    @Test
    public void  addUserState(){
        final String state = "1#5";
        final UUID userGuid = UUID.randomUUID();
        context.checking(new Expectations() {{
            oneOf(userStateDao).saveUserState(with(userGuid), with(state), with(any(DateTime.class)));
            oneOf(clock).getCurrentDateTime();
            will(returnValue(DateTime.now(DateTimeZone.UTC)));
        }});
        petriRpcServer.saveUserState(userGuid, state);


    }

    @Test
    public void getUserState() {
        final String state = "1#5";
        final UUID userGuid = UUID.randomUUID();
        context.checking(new Expectations() {{
            allowing(userStateDao).getUserState(userGuid);
            will(returnValue(state));
        }});
        assertThat(petriRpcServer.getUserState(userGuid), is(state));
    }

    @Test
    public void getFullUserState() {
        final String state = "1#5";
        final UUID userGuid = UUID.randomUUID();
        final DateTime currentDateTime = new DateTime();
        final UserState userState = new UserState(userGuid, state, currentDateTime);
        context.checking(new Expectations() {{
            allowing(userStateDao).getFullUserState(userGuid);
            will(returnValue(userState));
        }});
        assertThat(petriRpcServer.getFullUserState(userGuid), is(userState));
    }
}