package com.wixpress.petri.laboratory;

import com.google.common.collect.ImmutableMap;
import com.natpryce.makeiteasy.Maker;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.laboratory.converters.IntegerConverter;
import com.wixpress.petri.laboratory.converters.StringConverter;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import com.wixpress.petri.petri.*;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import scala.Option;
import scala.Some;

import java.util.*;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static com.wixpress.petri.experiments.domain.Experiment.InvalidExperiment;
import static com.wixpress.petri.laboratory.ConductionContextBuilder.newInstance;
import static com.wixpress.petri.laboratory.EligibilityCriteriaTypes.UserCreationDateCriterion;
import static com.wixpress.petri.laboratory.UserInfo.userInfoFromNullRequest;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.*;
import static com.wixpress.petri.laboratory.dsl.TestGroupMakers.*;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.*;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.UserInfo;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author sagyr
 * @since 8/7/13
 */

public class TrackableLaboratoryTest {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    public static final String FALLBACK_VALUE = "FALLBACK_VALUE";
    private CachedExperiments experiments;
    private static final UUID SOME_USER_GUID = UUID.fromString("19fc13d9-5943-4a87-82b1-4acb7e5cb039");
    private static final UUID OTHER_USER_GUID = UUID.fromString("19fc13d9-5943-4a87-82b1-4acb7e5cb041");
    private Maker<com.wixpress.petri.laboratory.UserInfo> aRegisteredUserInfo;
    public static int EXPERIMENT_MAX_TIME_MILLIS = 10;
    private UserInfo registeredUserInfo;


    private static final Class TheKey = new SpecDefinition() {
    }.getClass();

    private static final Class TheOtherKey = new SpecDefinition() {
    }.getClass();

    private static final String registeredKey = "for registered users";

    private final Maker<Experiment> experimentWithWinningFirstGroup = an(Experiment,
            with(id, 1),
            with(key, TheKey.getName()),
            with(testGroups, TEST_GROUPS_WITH_FIRST_ALWAYS_WINNING));

    private final Maker<Experiment> experimentForRegisteredUserWithWinningFirstGroup = experimentWithWinningFirstGroup.but(
            with(key, registeredKey),
            with(onlyForLoggedIn, true));

    private TrackableLaboratory lab;
    private RamUserInfoStorage userInfoStorage;
    private InMemoryExperimentsSource cache;
    private FakeTestGroupAssignmentTracker testGroupAssignmentTracker;
    private FakeErrorHandler laboratoryErrorHandler;
    private FakeMetricsReporter metricsReporter;
    private UserRequestPetriClient petriClient;
    private FakePetriTopology petriTopology;

    @Before
    public void setUp() throws Exception {
        cache = new InMemoryExperimentsSource();
        cache.write(new ArrayList<Experiment>());
        experiments = new CachedExperiments(cache);
        aRegisteredUserInfo = a(UserInfo,
                with(UserInfoMakers.userId, SOME_USER_GUID));
        registeredUserInfo = aRegisteredUserInfo.make();
        userInfoStorage = new RamUserInfoStorage();
        testGroupAssignmentTracker = new FakeTestGroupAssignmentTracker();
        laboratoryErrorHandler = new FakeErrorHandler();
        metricsReporter = new FakeMetricsReporter();
        petriTopology = new FakePetriTopology();

        petriClient = context.mock(UserRequestPetriClient.class);
        lab = new TrackableLaboratory(experiments, testGroupAssignmentTracker, userInfoStorage,
                laboratoryErrorHandler, EXPERIMENT_MAX_TIME_MILLIS, metricsReporter, petriClient, petriTopology);
    }

    public static class FakeErrorHandler implements ErrorHandler {

        private Throwable cause;

        @Override
        public void handle(String message, Throwable cause, ExceptionType exceptionType) {
            this.cause = cause;
        }

        private Throwable getCause() {
            return cause;
        }
    }

    public static class FakeTestGroupAssignmentTracker implements TestGroupAssignmentTracker {

        private List<Assignment> assignments = new ArrayList<>();

        @Override
        public void newAssignment(Assignment assignment) {
            this.assignments.add(assignment);
        }

        public List<Assignment> getAssignments() {
            return assignments;
        }

    }

    public static class FakeMetricsReporter implements MetricsReporter {

        Map<ReportKey, Integer> reportMap = new HashMap<>();

        @Override
        public void reportConductExperiment(int experimentId, String experimentValue) {
            reportMap.put(new ReportKey(experimentId, experimentValue), 1);
        }

        @Override
        public void reportToServer() {

        }

        public Map<ReportKey, Integer> getReportMap() {
            return reportMap;
        }
    }

    public static class FakePetriTopology implements PetriTopology {


        private boolean isWriteStateToServer = true;

        @Override
        public String getPetriUrl() {
            return null;
        }

        @Override
        public Long getReportsScheduleTimeInMillis() {
            return 30000l;
        }

        @Override
        public boolean isWriteStateToServer() {
            return isWriteStateToServer;
        }

        public void setWriteStateToServer(boolean isWriteStateToServer) {
            this.isWriteStateToServer = isWriteStateToServer;
        }
    }

    public void addExperimentToCache(Experiment experiment) {
        List<Experiment> currentExperiments = new ArrayList<Experiment>(this.cache.read());
        currentExperiments.add(experiment);
        this.cache.write(currentExperiments);
    }

    private void clearCache() {
        cache.write(new ArrayList<Experiment>());
    }

    private void assertBiLogHasItemThat(Matcher<Assignment> matcher) {
        assertThat(testGroupAssignmentTracker.getAssignments(), hasItem(matcher));
    }

    private void assertBiLogAndAnonAndUserLogsAreEmpty() {
        assertBiLogIsEmpty();
        assertUserLogIsEmpty();
        assertAnonymousLogIsEmpty();
    }

    private void assertBiLogIsEmpty() {
        assertThat(testGroupAssignmentTracker.getAssignments(), is(Matchers.empty()));
    }

    private void assertUserLogIsEmpty() {
        userInfoStorage.assertUserExperimentsLog("");
    }

    private void assertAnonymousLogIsEmpty() {
        userInfoStorage.assertAnonymousLogIs("");
    }

    private void conductByKeyAndByScopeReturns(String value) {
        conductByKeyAndByScopeReturns(value, newInstance());
    }

    private void conductByKeyAndByScopeReturns(final String value, ConductionContextBuilder context) {
        UserInfo prevUserInfo = userInfoStorage.read();
        assertThat(lab.conductExperiment(TheKey, "", context), is(value));

        //make sure cookies written by first conduction do not affect the second assertion
        userInfoStorage.write(prevUserInfo);

        Map<String, String> expected = new HashMap<String, String>() {{
            put(TheKey.getName(), value);
        }};
        assertThat(lab.conductAllInScope("someScope", context), is(expected));
    }

    private void errorReportWasSent(final Matcher<Throwable> exceptionMatcher) {
        assertThat(laboratoryErrorHandler.getCause(), is(exceptionMatcher));
    }

    private void noErrorReportsWereSent() {
        assertThat(laboratoryErrorHandler.getCause(), is(nullValue()));
    }

    private void writeUserInfoFromNullRequest() {
        writeUserInfoFromNullRequestOnHost("");
    }

    private void writeUserInfoFromNullRequestOnHost(String hostName) {
        UserInfo infoFromNullRequest = userInfoFromNullRequest(hostName);
        userInfoStorage.write(infoFromNullRequest);
    }

    private void returnUserStateFromServer(final String state) {
        returnUserStateFromServer(state, registeredUserInfo.getUserId());
    }

    private void returnUserStateFromServer(final String state, final UUID userId) {
        context.checking(new Expectations() {{
            allowing(petriClient).getUserState(userId);
            will(returnValue(state));
        }});
    }

    private Matcher<Assignment> containsAUserId(final UUID uid) {
        return new TypeSafeMatcher<Assignment>() {

            @Override
            protected boolean matchesSafely(Assignment assignment) {
                return assignment.getUserInfo().getUserId().equals(uid);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an Assignment with user id " + uid);
            }

        };
    }

    private Matcher<Assignment> containsTheUsersId() {
        return containsAUserId(SOME_USER_GUID);
    }

    private Matcher<Assignment> containsNoUserIdAndWinsWith(final int winningGroupId) {
        return new TypeSafeMatcher<Assignment>() {

            @Override
            protected boolean matchesSafely(Assignment assignment) {
                return assignment.getUserInfo().getUserId() == null && assignment.getTestGroup().getId() == winningGroupId;
            }

            @Override
            public void describeTo(Description description) {
            }

        };
    }


    Matcher<Throwable> anException(final Class<? extends Throwable> type, final Matcher<String> msg) {
        return new TypeSafeMatcher<Throwable>() {
            @Override
            protected boolean matchesSafely(Throwable throwable) {
                return type.equals(throwable.getClass()) && msg.matches(throwable.getMessage());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an exception of type: " + type + " with msg that " + description.appendDescriptionOf(msg));
            }
        };
    }

    @Test
    public void experimentIsNotAppendedToLogTwiceIfAlreadyRunningAndExistingValueIsReturned() throws Exception {
        addExperimentToCache(experimentWithWinningFirstGroup.make());
        userInfoStorage.write(aRegisteredUserInfo.but(with(anonymousExperimentsLog, "1#2")).make());
        String result = lab.conductExperiment(TheKey, FALLBACK_VALUE);

        userInfoStorage.assertAnonymousLogIs("1#2");
        assertThat(result, is(LOSING_VALUE));
        assertBiLogIsEmpty();
        assertUserLogIsEmpty();
    }

    @Test
    public void returnsTheDrawnTestGroupValue() throws Exception {
        addExperimentToCache(experimentWithWinningFirstGroup.make());
        userInfoStorage.write(aRegisteredUserInfo.make());
        assertThat(lab.conductExperiment(TheKey, FALLBACK_VALUE), is(WINNING_VALUE));
    }

    @Test
    public void removesExpiredExperimentsAndAppendsNewOnes() throws Exception {
        DateTime now = new DateTime();
        Maker<Experiment> expiredExperiment = experimentWithWinningFirstGroup.but(
                with(endDate, now.minusHours(1)),
                with(startDate, now.minusHours(2)));
        Maker<Experiment> newExperiment = expiredExperiment.but(
                with(id, 2),
                with(startDate, now),
                with(endDate, now.plusYears(1)),
                with(key, TheOtherKey.getName()));

        addExperimentToCache(expiredExperiment.make());
        addExperimentToCache(newExperiment.make());

        userInfoStorage.write(aRegisteredUserInfo.but(with(experimentsLog, "1#2")).make());
        lab.conductExperiment(TheOtherKey, FALLBACK_VALUE);
        userInfoStorage.assertAnonymousLogIs("2#1");
    }

    @Test
    public void appendsBILogEntryWhenNewTestIsDrawn() throws Exception {
        addExperimentToCache(experimentWithWinningFirstGroup.make());
        userInfoStorage.write(aRegisteredUserInfo.make());
        lab.conductExperiment(TheKey, FALLBACK_VALUE);
        assertBiLogHasItemThat(allOf(
                containsTheUsersId()));
    }

    @Test
    public void experimentIsNotConductedIfFiltersDontMatch() throws Exception {
        Experiment experimentForAnonymousUsers = experimentWithWinningFirstGroup.but(
                with(filters, new ArrayList<Filter>() {{
                    add(new FirstTimeVisitorsOnlyFilter());
                }})
        ).make();

        userInfoStorage.write(aRegisteredUserInfo.make());
        addExperimentToCache(experimentForAnonymousUsers);

        assertThat(lab.conductExperiment(TheKey, FALLBACK_VALUE), is(FALLBACK_VALUE));
        assertBiLogAndAnonAndUserLogsAreEmpty();
    }

    @Test
    public void experimentWithFiltersIsConductedIfFiltersMatch() throws Exception {
        Experiment experimentForAnonymousUsers = experimentWithWinningFirstGroup.but(
                with(filters, new ArrayList<Filter>() {{
                    add(new FirstTimeVisitorsOnlyFilter());
                }})
        ).make();

        UserInfo anonUser = AnonymousUserInfo.but(with(experimentsLog, "")).make();
        userInfoStorage.write(anonUser);
        addExperimentToCache(experimentForAnonymousUsers);

        lab.conductExperiment(TheKey, FALLBACK_VALUE);
        userInfoStorage.assertAnonymousLogIs("1#1");
    }


    @Test
    public void canConductNonStringExperiments() throws Exception {
        Experiment experimentWithNonStringTestGroupValue = experimentWithWinningFirstGroup.but(
                with(testGroups, listOf(
                        a(TestGroup,
                                with(value, "123"),
                                with(probability, 100)),
                        a(TestGroup,
                                with(probability, 0))
                ))
        ).make();
        addExperimentToCache(experimentWithNonStringTestGroupValue);
        userInfoStorage.write(aRegisteredUserInfo.make());
        assertThat(lab.conductExperiment(TheKey, -1, new IntegerConverter()), is(123));
    }

    @Test
    public void conductsSingleExperimentInScope() throws Exception {
        Experiment experimentInEditorScope = experimentWithWinningFirstGroup.but(
                with(scope, "editor")).make();
        addExperimentToCache(experimentInEditorScope);
        userInfoStorage.write(aRegisteredUserInfo.make());
        Map<String, String> expected = new HashMap<String, String>();
        expected.put(TheKey.getName(), WINNING_VALUE);
        assertThat(lab.conductAllInScope("editor"), is(expected));
        userInfoStorage.assertAnonymousLogIs("1#1");
    }

    @Test
    public void conductsSomeExperimentsInScope() throws Exception {
        Experiment editorExperiment1 =
                an(Experiment,
                        with(id, 1),
                        with(key, "key1"),
                        with(scope, "editor"),
                        with(testGroups, TEST_GROUPS_WITH_FIRST_ALWAYS_WINNING)).make();

        Experiment editorExperiment2 =
                an(Experiment,
                        with(id, 2),
                        with(key, "key2"),
                        with(scope, "editor"),
                        with(testGroups, TEST_GROUPS_WITH_FIRST_ALWAYS_WINNING)).make();

        addExperimentToCache(editorExperiment1);
        addExperimentToCache(editorExperiment2);

        Map<String, String> expected = new HashMap<String, String>();
        userInfoStorage.write(aRegisteredUserInfo.make());
        expected.put("key1", WINNING_VALUE);
        expected.put("key2", WINNING_VALUE);
        assertThat(lab.conductAllInScope("editor"), is(expected));
        userInfoStorage.assertAnonymousLogIs("1#1|2#1");
    }

    @Test
    public void returnsDefaultValueIfNoExperimentFoundWithGivenKey() {
        Class<SpecDefinition> nonExistingExperimentKey = SpecDefinition.class;
        userInfoStorage.write(aRegisteredUserInfo.make());
        assertThat(lab.conductExperiment(nonExistingExperimentKey, FALLBACK_VALUE), is(FALLBACK_VALUE));
    }

    @Test
    public void whenUserIsNotEligibleExperimentInScopeIsNotConducted() throws Exception {
        ArrayList<Filter> filterGeoGb = new ArrayList<Filter>();
        filterGeoGb.add(new GeoFilter(asList("gb")));
        Experiment experimentWithGeoGbFilter = experimentWithWinningFirstGroup.but(
                with(scope, "editor"),
                with(filters, filterGeoGb)).make();
        addExperimentToCache(experimentWithGeoGbFilter);

        Maker<UserInfo> aUserNotFromGb = aRegisteredUserInfo.but(with(country, "not-gb"));
        userInfoStorage.write(aUserNotFromGb.make());

        Map<String, String> expected = new HashMap<String, String>();
        assertThat(lab.conductAllInScope("editor"), is(expected));
        assertBiLogAndAnonAndUserLogsAreEmpty();
    }

    @Test
    public void expiredExperimentsAreNotRemovedWhenExperimentsAreEmpty() throws Exception {
        addExperimentToCache(experimentWithWinningFirstGroup.make());
        userInfoStorage.write(aRegisteredUserInfo.make());
        lab.conductExperiment(TheKey, "");
        clearCache();
        lab.conductExperiment(TheKey, "");
        userInfoStorage.assertAnonymousLogIs("1#1");
    }

    @Test
    public void expiredExperimentsAreRemovedWhenConductingAllInScope() throws Exception {
        userInfoStorage.write(aRegisteredUserInfo.but(with(experimentsLog, "4#5"), with(anonymousExperimentsLog, "5#5")).make());
        Maker<Experiment> experimentInEditorScope = experimentWithWinningFirstGroup.but(
                with(scope, "editor"));
        addExperimentToCache(experimentInEditorScope.make());
        lab.conductAllInScope("editor");

        assertUserLogIsEmpty();
        userInfoStorage.assertAnonymousLogIs("1#1");
    }


    @Test
    public void pausedExperimentsAreNotRemoved() {
        Maker<Experiment> pausedExperimentInEditorScope = experimentWithWinningFirstGroup.but(
                with(scope, "editor"),
                with(paused, true));
        addExperimentToCache(pausedExperimentInEditorScope.make());
        userInfoStorage.write(aRegisteredUserInfo.but(with(anonymousExperimentsLog, "1#1")).make());

        lab.conductAllInScope("editor");
        userInfoStorage.assertAnonymousLogIs("1#1");
    }

    @Test
    public void pausedExperimentsAreNotAdded() {
        Experiment pausedExperimentInEditorScope = experimentWithWinningFirstGroup.but(
                with(scope, "editor"),
                with(paused, true)).make();
        addExperimentToCache(pausedExperimentInEditorScope);
        userInfoStorage.write(aRegisteredUserInfo.make());

        lab.conductAllInScope("editor");
        assertBiLogAndAnonAndUserLogsAreEmpty();
    }

    @Test
    public void whenConductingSeveralExperimentsOnSameKeyFirstEligibleOneWins() {
        ArrayList<Filter> filterGeoGb = new ArrayList<Filter>();
        filterGeoGb.add(new GeoFilter(asList("gb")));

        Maker<Experiment> experiment = experimentWithWinningFirstGroup.but(
                with(scope, "someScope"));

        Maker<UserInfo> aUserNotFromGb = aRegisteredUserInfo.but(with(country, "not-gb"));
        userInfoStorage.write(aUserNotFromGb.make());

        addExperimentToCache(experiment.but(with(filters, filterGeoGb)).make());
        addExperimentToCache(experiment.but(with(id, 2)).make());
        addExperimentToCache(experiment.but(with(id, 3), with(testGroups, TEST_GROUPS_WITH_SECOND_ALWAYS_WINNING)).make());

        conductByKeyAndByScopeReturns(WINNING_VALUE);
        userInfoStorage.assertAnonymousLogIs("2#1");
    }


    @Test
    public void featureToggleExperimentIsNotNotifiedToBIOrAddedToCookies() throws Exception {
        Experiment featureToggleExperiment = experimentWithWinningFirstGroup.but(
                with(scope, "someScope"),
                with(featureToggle, true)).make();

        userInfoStorage.write(aRegisteredUserInfo.make());
        addExperimentToCache(featureToggleExperiment);

        conductByKeyAndByScopeReturns(WINNING_VALUE);
        assertBiLogAndAnonAndUserLogsAreEmpty();
    }

    @Test
    public void defaultValueIsReturnedWhenUnexpectedExceptionIsThrown() {
        experiments = new BlowingUpCachedExperiments();
        userInfoStorage.write(aRegisteredUserInfo.make());
        lab = new TrackableLaboratory(experiments, new FakeTestGroupAssignmentTracker(), userInfoStorage,
                laboratoryErrorHandler, EXPERIMENT_MAX_TIME_MILLIS, metricsReporter, petriClient, petriTopology);
        assertThat(lab.conductExperiment(TheKey, FALLBACK_VALUE), is(FALLBACK_VALUE));
        errorReportWasSent(Matchers.<Throwable>instanceOf(BlowingUpCachedExperiments.CacheExploded.class));
        assertBiLogIsEmpty();
    }

    @Test
    public void defaultValuesAreReturnedWhenUnexpectedExceptionIsThrown() {
        experiments = new BlowingUpCachedExperiments();
        userInfoStorage.write(aRegisteredUserInfo.make());
        lab = new TrackableLaboratory(experiments, new FakeTestGroupAssignmentTracker(), userInfoStorage,
                laboratoryErrorHandler, EXPERIMENT_MAX_TIME_MILLIS, metricsReporter, petriClient, petriTopology);

        Map<String, String> expected = new HashMap();
        assertThat(lab.conductAllInScope("whatever"), is(expected));
        errorReportWasSent(Matchers.<Throwable>instanceOf(BlowingUpCachedExperiments.CacheExploded.class));
        assertBiLogIsEmpty();
    }

    @Test
    public void conductByScopeReturnsPartialMapWhenSomeThrowExceptions() {
        Maker<Experiment> nonBlowingUpExperiment = experimentWithWinningFirstGroup.but(with(scope, "someScope"));

        Maker<Experiment> blowingUpExperiment = nonBlowingUpExperiment.but(
                with(filters, new ArrayList<Filter>() {{
                    add(new BlowingUpFilter());
                }})
        );

        userInfoStorage.write(aRegisteredUserInfo.make());
        addExperimentToCache(blowingUpExperiment.make());
        addExperimentToCache(nonBlowingUpExperiment.make());

        Map<String, String> expected = new HashMap() {{
            put(TheKey.getName(), WINNING_VALUE);
        }};
        assertThat(lab.conductAllInScope("someScope"), is(expected));
        errorReportWasSent(Matchers.<Throwable>instanceOf(BlowingUpFilter.FilterExploded.class));
    }


    @Test
    public void defaultValuesAreReturnedWhenNoUserInfoExists() throws Exception {
        addExperimentToCache(experimentWithWinningFirstGroup.make());
        writeUserInfoFromNullRequest();

        final Matcher<Throwable> matcher = anException(UnsupportedOperationException.class, containsString("non-http flow"));

        assertThat(lab.conductExperiment(TheKey, FALLBACK_VALUE), is(FALLBACK_VALUE));
        errorReportWasSent(matcher);

        assertBiLogAndAnonAndUserLogsAreEmpty();
    }

    @Test
    public void canConductWithNoSpec() throws Exception {
        addExperimentToCache(experimentWithWinningFirstGroup.make());
        userInfoStorage.write(aRegisteredUserInfo.make());
        assertThat(lab.conductExperiment(TheKey, FALLBACK_VALUE, new StringConverter()), is(WINNING_VALUE));
    }

    @Test
    public void appendsToUserLogWhenExperimentIsForRegisteredUsers() throws Exception {
        final Experiment registeredUsersExperiment = experimentForRegisteredUserWithWinningFirstGroup.make();

        addExperimentToCache(registeredUsersExperiment);

        userInfoStorage.write(registeredUserInfo);
        returnUserStateFromServer("");
        lab.conductExperiment(registeredKey, "", new StringConverter());

        userInfoStorage.assertUserExperimentsLog("1#1");
        userInfoStorage.assertAnonymousLogIs("");
    }

    @Test
    public void conductedExperimentIsConcatenatedToExisting() throws Exception {
        final Experiment registeredUsersExperiment = experimentForRegisteredUserWithWinningFirstGroup.make();
        addExperimentToCache(registeredUsersExperiment);

        addExperimentToCache(experimentWithWinningFirstGroup.but(with(id, 3)).make());

        final UserInfo registeredUserInfo = aRegisteredUserInfo.but(with(experimentsLog, "3#2")).make();

        userInfoStorage.write(registeredUserInfo);
        returnUserStateFromServer("");

        lab.conductExperiment(registeredKey, "", new StringConverter());

        userInfoStorage.assertUserExperimentsLog("3#2|1#1");
    }


    @Test
    public void doNotReadStateFromServerWhenExperimentIsForRegisteredUsersAndConfigIsOff() throws Exception {
        addExperimentToCache(experimentForRegisteredUserWithWinningFirstGroup.make());
        petriTopology.setWriteStateToServer(false);

        userInfoStorage.write(registeredUserInfo);
        //no expectation allowing reading state from server
        lab.conductExperiment(registeredKey, "", new StringConverter());
    }

    @Test
    public void experimentIsNotConductedButAppendedToLogIfAlreadyInServerForRegisteredUsers() throws Exception {
        final Experiment registeredUsersExperiment = experimentForRegisteredUserWithWinningFirstGroup.make();
        addExperimentToCache(registeredUsersExperiment);

        userInfoStorage.write(registeredUserInfo);
        returnUserStateFromServer("1#2");

        String result = lab.conductExperiment(registeredKey, "", new StringConverter());

        assertThat(result, is(LOSING_VALUE));
        assertBiLogIsEmpty();
        assertAnonymousLogIsEmpty();
        userInfoStorage.assertUserExperimentsLog("1#2");
    }


    @Test
    public void experimentIsNotReadFromServerForFTAndRegisteredUsers() throws Exception {
        final Experiment registeredUsersFT = experimentForRegisteredUserWithWinningFirstGroup.but(
                with(featureToggle, true)).make();
        addExperimentToCache(registeredUsersFT);

        userInfoStorage.write(registeredUserInfo);

        lab.conductExperiment(registeredKey, "", new StringConverter());
    }

    @Test
    public void experimentIsNotReadFromServerForRegisteredUsersWhenCookieExists() throws Exception {
        final Experiment registeredUsersExperiment = experimentForRegisteredUserWithWinningFirstGroup.make();
        addExperimentToCache(registeredUsersExperiment);

        final UserInfo registeredUserInfoWithCookie = aRegisteredUserInfo.but(with(experimentsLog, "1#2")).make();
        userInfoStorage.write(registeredUserInfoWithCookie);

        lab.conductExperiment(registeredKey, "", new StringConverter());
    }

    @Test
    public void experimentIsNotReadFromServerWhenCookieExistsEvenWhenOtherFTExistsOnKey() throws Exception {
        final Experiment registeredUsersExperiment = experimentForRegisteredUserWithWinningFirstGroup.make();
        final Experiment registeredUsersFT = experimentForRegisteredUserWithWinningFirstGroup.but(with(id, 2), with(featureToggle, true)).make();

        addExperimentToCache(registeredUsersExperiment);
        addExperimentToCache(registeredUsersFT);

        final UserInfo registeredUserInfo = aRegisteredUserInfo.but(with(experimentsLog, "1#2")).make();
        userInfoStorage.write(registeredUserInfo);

        lab.conductExperiment(registeredKey, "", new StringConverter());
    }

    @Test
    public void experimentIsNotReadFromServerWhenCookieExistsEvenWhenOtherABTestExistsOnKey() throws Exception {
        final Experiment registeredUsersExperiment = experimentForRegisteredUserWithWinningFirstGroup.make();
        final Experiment registeredUsersExperiment2 = experimentForRegisteredUserWithWinningFirstGroup.but(with(id, 2)).make();

        addExperimentToCache(registeredUsersExperiment);
        addExperimentToCache(registeredUsersExperiment2);

        final UserInfo registeredUserInfo = aRegisteredUserInfo.but(with(experimentsLog, "1#2")).make();
        userInfoStorage.write(registeredUserInfo);

        lab.conductExperiment(registeredKey, "", new StringConverter());
    }


    @Test
    public void experimentDoesntFailWhenReadingFromServerFails() throws Exception {
        final Experiment registeredUsersExperiment = experimentForRegisteredUserWithWinningFirstGroup.make();
        addExperimentToCache(registeredUsersExperiment);

        userInfoStorage.write(registeredUserInfo);
        context.checking(new Expectations() {{
            allowing(petriClient).getUserState(registeredUserInfo.getUserId());
            will(throwException(new NullPointerException()));
        }});
        lab.conductExperiment(registeredKey, "", new StringConverter());

        userInfoStorage.assertUserExperimentsLog("1#1");
        userInfoStorage.assertAnonymousLogIs("");

        final Matcher<Throwable> matcher = Matchers.instanceOf(NullPointerException.class);
        errorReportWasSent(matcher);
    }


    @Test
    public void conductByScopeReadsServerStateForRegisteredUsers() throws Exception {
        final Experiment registeredUserExperimentInEditorScope = experimentForRegisteredUserWithWinningFirstGroup.but(
                with(scope, "editor")).make();
        addExperimentToCache(registeredUserExperimentInEditorScope);

        userInfoStorage.write(registeredUserInfo);
        returnUserStateFromServer("1#2");

        Map<String, String> expected = ImmutableMap.of(registeredKey, LOSING_VALUE);
        assertThat(lab.conductAllInScope("editor"), is(expected));
        userInfoStorage.assertUserExperimentsLog("1#2");
    }

    @Test
    public void defaultValuesAreReturnedForRobots() throws Exception {
        addExperimentToCache(experimentWithWinningFirstGroup.but(with(scope, "someScope")).make());
        userInfoStorage.write(a(UserInfo, with(robot, true)).make());

        assertThat(lab.conductExperiment(TheKey, FALLBACK_VALUE), is(FALLBACK_VALUE));

        Map<String, String> expected = new HashMap();
        assertThat(lab.conductAllInScope("someScope"), is(expected));

        assertBiLogAndAnonAndUserLogsAreEmpty();
    }

    @Test
    public void featureToggleIgnoresPreviouslyConductedValues() {
        Experiment firstWinningExperiment = experimentWithWinningFirstGroup.but(with(featureToggle, true)).make();

        addExperimentToCache(firstWinningExperiment);
        userInfoStorage.write(AnonymousUserInfo.but(with(anonymousExperimentsLog, "1#2")).make());

        assertThat(lab.conductExperiment(TheKey, "fallback"), is(WINNING_VALUE));
    }

    @Test
    public void whenConductingSeveralExperimentsOnSameKeyFtsHavePrecedence() {
        Maker<Experiment> experiment = experimentWithWinningFirstGroup.but(with(scope, "someScope"));
        Maker<Experiment> ft = experiment.but(
                with(id, 2),
                with(featureToggle, true),
                with(testGroups, TEST_GROUPS_WITH_SECOND_ALWAYS_WINNING));

        userInfoStorage.write(aRegisteredUserInfo.make());

        addExperimentToCache(experiment.make());
        addExperimentToCache(ft.make());

        conductByKeyAndByScopeReturns(LOSING_VALUE);
    }

    @Test
    public void errorIsReportedIfTryingToConductInvalidExperiment() {
        userInfoStorage.write(aRegisteredUserInfo.make());
        final UnrecognizedFilter unrecognizedFilter = new UnrecognizedFilter();
        Maker<Experiment> experimentWithUnknownFilter = experimentWithWinningFirstGroup.
                but(with(filters, asList(unrecognizedFilter, new FirstTimeVisitorsOnlyFilter())));
        addExperimentToCache(experimentWithUnknownFilter.make());

        final Matcher<Throwable> matcher = anException(InvalidExperiment.class, allOf(
                containsString(unrecognizedFilter.toString()),
                containsString("too old")));

        assertThat(lab.conductExperiment(TheKey, FALLBACK_VALUE), is(FALLBACK_VALUE));
        errorReportWasSent(matcher);
        assertBiLogAndAnonAndUserLogsAreEmpty();
    }

    @Test
    public void nullUserInfoGetsFTValue() {
        addExperimentToCache(experimentWithWinningFirstGroup.but(
                with(testGroups, TEST_GROUPS_WITH_SECOND_ALWAYS_WINNING),
                with(featureToggle, true)
        ).make());

        writeUserInfoFromNullRequest();

        assertThat(lab.conductExperiment(TheKey, FALLBACK_VALUE), is(LOSING_VALUE));
    }

    @Test
    public void nullUserInfoCanUseHostFilter() {
        final String host = "some host";

        addExperimentToCache(experimentWithWinningFirstGroup.but(
                with(testGroups, TEST_GROUPS_WITH_SECOND_ALWAYS_WINNING),
                with(featureToggle, true),
                with(filters, new ArrayList<Filter>() {{
                    add(new HostFilter(asList(host)));
                }})
        ).make());

        writeUserInfoFromNullRequestOnHost(host);

        assertThat(lab.conductExperiment(TheKey, FALLBACK_VALUE), is(LOSING_VALUE));
    }

    @Test
    public void filterCanUseCustomEligibilityField() throws Exception {
        List<Filter> customFilter = new ArrayList<>();
        customFilter.add(new Filter() {
            @Override
            public boolean isEligible(EligibilityCriteria eligibilityCriteria) {
                return eligibilityCriteria.getAdditionalCriterion(StringCriterion.class).equals("someString");
            }
        });
        Experiment experimentWithCustomFilter = experimentWithWinningFirstGroup.but(with(filters, customFilter)).make();
        addExperimentToCache(experimentWithCustomFilter);

        userInfoStorage.write(aRegisteredUserInfo.make());
        assertThat(lab.conductExperiment(TheKey, FALLBACK_VALUE), is(FALLBACK_VALUE));

        userInfoStorage.write(aRegisteredUserInfo.make());
        assertThat(lab.conductExperiment(TheKey, FALLBACK_VALUE, newInstance().withCriterionOverride(new StringCriterion())),
                is(WINNING_VALUE));
    }

    @Test(expected = NullPointerException.class)
    public void passingNullEligibilityFieldThrowsNPE() throws Exception {
        userInfoStorage.write(aRegisteredUserInfo.make());
        lab.conductExperiment(TheKey, FALLBACK_VALUE, newInstance().withCriterionOverride(null));
    }

    private class StringCriterion implements EligibilityCriterion<String> {
        @Override
        public String getValue() {
            return "someString";
        }
    }

    @Test
    public void reportErrorWhenExperimentIsSlow() throws Exception {

        Experiment experimentWithSlowCalculationTime = experimentWithWinningFirstGroup.but(
                with(filters, new ArrayList<Filter>() {{
                    add(new SlowFilter());
                }})
        ).make();

        userInfoStorage.write(aRegisteredUserInfo.make());
        addExperimentToCache(experimentWithSlowCalculationTime);

        lab.conductExperiment(TheKey, FALLBACK_VALUE);

        final Matcher<Throwable> matcher = anException(SlowExperimentException.class, allOf(
                        containsString("Slow Conducting time of experiment"),
                        containsString("Experiment{id=" + experimentWithSlowCalculationTime.getId()))
        );

        errorReportWasSent(matcher);
    }

    @Test
    public void eligibilityFieldsCanBeOverridenOnConduct() throws Exception {
        Experiment experimentOnNewUsers = experimentWithWinningFirstGroup.but(
                with(scope, "someScope"),
                with(filters, new ArrayList<Filter>() {{
                    add(new NewUsersFilter());
                }})
        ).make();
        addExperimentToCache(experimentOnNewUsers);

        UserInfo oldUserInfo = aRegisteredUserInfo.but(with(dateCreated, new DateTime().minusHours(1))).make();
        userInfoStorage.write(oldUserInfo);
        assertThat(lab.conductExperiment(TheKey, FALLBACK_VALUE), is(FALLBACK_VALUE));

        ConductionContextBuilder context = newInstance().withCriterionOverride(new UserCreationDateCriterion(new DateTime()));

        conductByKeyAndByScopeReturns(WINNING_VALUE, context);
    }

    @Test
    public void experimentIsReportedWhenConducted() throws Exception {
        Experiment anExperiment = experimentWithWinningFirstGroup.make();
        addExperimentToCache(anExperiment);
        userInfoStorage.write(AnonymousUserInfo.make());

        lab.conductExperiment(TheKey, FALLBACK_VALUE);

        assertThat(metricsReporter.getReportMap(), hasEntry(new ReportKey(anExperiment.getId(), WINNING_VALUE), 1));
    }


    @Test
    public void experimentIsNotReportedWhenTheExperimentIsPaused() throws Exception {
        Experiment pausedExperiment = experimentWithWinningFirstGroup.but(
                with(paused, true)
        ).make();

        userInfoStorage.write(AnonymousUserInfo.make());
        addExperimentToCache(pausedExperiment);

        lab.conductExperiment(TheKey, FALLBACK_VALUE);

        assertThat(metricsReporter.getReportMap().isEmpty(), is(true));

        noErrorReportsWereSent();

    }

    @Test
    public void persistentKernelFromCustomConductionStrategyIsUsedForConduction() throws Exception {
        final Experiment experiment = experimentForRegisteredUserWithWinningFirstGroup.make();
        addExperimentToCache(experiment);

        userInfoStorage.write(aRegisteredUserInfo.make());

        returnUserStateFromServer("", OTHER_USER_GUID);
        final ConductionStrategy customStrategyWithKernel = context.mock(ConductionStrategy.class);
        context.checking(new Expectations() {{
            allowing(customStrategyWithKernel).shouldPersist();
            will(returnValue(true));
            allowing(customStrategyWithKernel).persistentKernel();
            will(returnValue(new Some(OTHER_USER_GUID)));
            allowing(customStrategyWithKernel).getUserIdRepresentedForFlow(with(any(Option.class)));
            will(returnValue(new Some(OTHER_USER_GUID)));
            allowing(customStrategyWithKernel).drawTestGroup(experiment);
            will(returnValue(experiment.getTestGroupById(2)));
        }});
        ConductionContext contextWithCustomKernel = newInstance().withConductionStrategy(customStrategyWithKernel);

        assertThat(lab.conductExperiment(registeredKey, "-1", new StringConverter(), contextWithCustomKernel), is(LOSING_VALUE));
        assertBiLogHasItemThat(allOf(containsTheUsersId()));
        assertAnonymousLogIsEmpty();
        assertUserLogIsEmpty();
        userInfoStorage.assertUserExperimentsLog(OTHER_USER_GUID, "1#2");
    }

    @Test
    public void persistentKernelFromCustomConductionStrategyIsUsedToReadFromCookie() throws Exception {
        final Experiment experiment = experimentForRegisteredUserWithWinningFirstGroup.make();
        addExperimentToCache(experiment);

        userInfoStorage.write(aRegisteredUserInfo.but(with(otherUserExperimentsLog, new HashMap<UUID, String>() {{
            put(OTHER_USER_GUID, "1#2");
        }})).make());

        final ConductionStrategy customStrategyWithKernel = context.mock(ConductionStrategy.class);
        context.checking(new Expectations() {{
            allowing(customStrategyWithKernel).persistentKernel();
            will(returnValue(new Some(OTHER_USER_GUID)));
        }});
        ConductionContext contextWithCustomKernel = newInstance().withConductionStrategy(customStrategyWithKernel);

        assertThat(lab.conductExperiment(registeredKey, "-1", new StringConverter(), contextWithCustomKernel), is(LOSING_VALUE));
        assertBiLogAndAnonAndUserLogsAreEmpty();
        userInfoStorage.assertUserExperimentsLog(OTHER_USER_GUID, "1#2");
    }


    @Test
    public void customTestGroupDrawerFromCustomConductionStrategyCanBeUsedWithNoUser() throws Exception {
        final Experiment experiment = experimentForRegisteredUserWithWinningFirstGroup.make();
        addExperimentToCache(experiment);

        writeUserInfoFromNullRequest();

        //(no need to allow server state read)
        final ConductionStrategy customStrategyWithNoKernel = context.mock(ConductionStrategy.class);
        context.checking(new Expectations() {{
            allowing(customStrategyWithNoKernel).shouldPersist();
            will(returnValue(false));
            allowing(customStrategyWithNoKernel).persistentKernel();
            will(returnValue(scala.Option.apply(null)));
            allowing(customStrategyWithNoKernel).getUserIdRepresentedForFlow(with(any(Option.class)));
            will(returnValue(scala.Option.apply(null)));
            allowing(customStrategyWithNoKernel).drawTestGroup(experiment);
            will(returnValue(experiment.getTestGroupById(2)));
        }});
        ConductionContext contextWithCustomKernel = newInstance().withConductionStrategy(customStrategyWithNoKernel);

        assertThat(lab.conductExperiment(registeredKey, FALLBACK_VALUE, new StringConverter(), contextWithCustomKernel), is(LOSING_VALUE));
        assertAnonymousLogIsEmpty();
        assertUserLogIsEmpty();
        userInfoStorage.assertUserExperimentsLogIsEmpty();
        assertBiLogHasItemThat(containsNoUserIdAndWinsWith(2));
    }

    @Test
    public void appendsBILogEntryButDoesNotPersistWhenCustomConductionStrategySaysSo() throws Exception {
        final Experiment experiment = experimentWithWinningFirstGroup.make();
        addExperimentToCache(experiment);

        userInfoStorage.write(aRegisteredUserInfo.make());

        final ConductionStrategy customStrategyWithNoPersistence = context.mock(ConductionStrategy.class);
        context.checking(new Expectations() {{
            allowing(customStrategyWithNoPersistence).shouldPersist();
            will(returnValue(false));
            allowing(customStrategyWithNoPersistence).persistentKernel();
            will(returnValue(scala.Option.apply(null)));
            allowing(customStrategyWithNoPersistence).getUserIdRepresentedForFlow(with(any(Option.class)));
            will(returnValue(scala.Option.apply(null)));
            allowing(customStrategyWithNoPersistence).drawTestGroup(experiment);
            will(returnValue(experiment.getTestGroupById(2)));
        }});
        ConductionContext contextWithCustomKernel = newInstance().withConductionStrategy(customStrategyWithNoPersistence);
        lab.conductExperiment(TheKey, FALLBACK_VALUE, contextWithCustomKernel);

        assertBiLogHasItemThat(allOf(containsTheUsersId()));
        assertAnonymousLogIsEmpty();
        assertUserLogIsEmpty();
    }

    private ConductionContext conductionContextByOtherUser(final Experiment exp, final UUID userGuid) {
        final ConductionStrategy strategy = context.mock(ConductionStrategy.class);

        context.checking(new Expectations() {{
            allowing(strategy).persistentKernel();
            will(returnValue(new Some(userGuid)));
            allowing(strategy).shouldPersist();
            allowing(strategy).getUserIdRepresentedForFlow(with(any(Option.class)));
            will(returnValue(new Some(userGuid)));
            allowing(strategy).drawTestGroup(exp);
            will(returnValue(exp.getTestGroupById(1)));
        }});
        return newInstance().withConductionStrategy(strategy);
    }



    @Test
    public void userIdCanBeUsedFromConductionStrategyForIncludeSpecificUserIdFilterToo() throws Exception {
        Experiment experimentOnSomeUser = experimentWithWinningFirstGroup.but(
                with(filters, new ArrayList<Filter>() {{
                    add(new IncludeUserIdsFilter(OTHER_USER_GUID));
                }})
        ).make();
        addExperimentToCache(experimentOnSomeUser);

        UserInfo someUserInfo = aRegisteredUserInfo.make();
        userInfoStorage.write(someUserInfo);

        assertThat(lab.conductExperiment(TheKey, FALLBACK_VALUE,
                        conductionContextByOtherUser(experimentOnSomeUser, OTHER_USER_GUID)),
                is(WINNING_VALUE));
    }

    @Test
    public void userIdCanBeUsedFromConductionStrategyForRegisteredUsersFilterTooEvenIfNoUserInSession() throws Exception {
        Experiment experimentOnSomeUser = experimentForRegisteredUserWithWinningFirstGroup.but(
                with(filters, new ArrayList<Filter>() {{
                    add(new RegisteredUsersFilter());
                }})
        ).make();
        addExperimentToCache(experimentOnSomeUser);

        userInfoStorage.write(AnonymousUserInfo.make());
        //no user in session but because we want to conduct by user state is still searched
        //(which is cool, it means even if behind queue/offline/any other scenario where no http request db state is still used)
        returnUserStateFromServer("", OTHER_USER_GUID);

        assertThat(lab.conductExperiment(registeredKey, FALLBACK_VALUE, new StringConverter(),
                        conductionContextByOtherUser(experimentOnSomeUser, OTHER_USER_GUID)),
                is(WINNING_VALUE));
    }

    //some strategy that doesnt represent a user (for example conducting by site id)
    //TODO - implemented as a hand rolled stub instead of a mock like other tests on conductionStrategy because we need to capture the argument userInSession and its too annoying with jmock
    //decide if they should all be mocks or stubs and consolidate
    private ConductionContext conductionContextBySiteId(final UUID siteId) {
        ConductionStrategy strategy = new ConductionStrategy() {
            @Override
            public Option<UUID> persistentKernel() {
                return new Some<>(siteId);
            }

            @Override
            public boolean shouldPersist() {
                return true;
            }

            @Override
            public Option<UUID> getUserIdRepresentedForFlow(Option<UUID> userInSession) {
                return  userInSession;
            }

            @Override
            public TestGroup drawTestGroup(Experiment exp) {
                return exp.getTestGroupById(1);
            }
        };

        return newInstance().withConductionStrategy(strategy);

    }

    @Test
    public void registeredUsersFilterWorksEvenWhenConductionStrategyHasAnIdThatDoesNotRepresentUsers() throws Exception {
        Experiment experimentOnSomeUser = experimentForRegisteredUserWithWinningFirstGroup.but(
                with(filters, new ArrayList<Filter>() {{
                    add(new RegisteredUsersFilter());
                }})
        ).make();
        addExperimentToCache(experimentOnSomeUser);

        UUID siteId = UUID.randomUUID();
        returnUserStateFromServer("", siteId);

        ConductionContext conductionContextBySiteId = conductionContextBySiteId(siteId);

        userInfoStorage.write(AnonymousUserInfo.make());
        assertThat(lab.conductExperiment(registeredKey, FALLBACK_VALUE, new StringConverter(),
                        conductionContextBySiteId),
                is(FALLBACK_VALUE));

        userInfoStorage.write(aRegisteredUserInfo.make());
        assertThat(lab.conductExperiment(registeredKey, FALLBACK_VALUE, new StringConverter(),
                        conductionContextBySiteId),
                is(WINNING_VALUE));
    }



}
