package com.wixpress.petri.laboratory;

import com.natpryce.makeiteasy.Maker;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.laboratory.converters.IntegerConverter;
import com.wixpress.petri.laboratory.converters.StringConverter;
import com.wixpress.petri.laboratory.dsl.UserInfoMakers;
import com.wixpress.petri.petri.SpecDefinition;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.*;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static com.wixpress.petri.experiments.domain.Experiment.InvalidExperiment;
import static com.wixpress.petri.laboratory.UserInfo.userInfoFromNullRequest;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.*;
import static com.wixpress.petri.laboratory.dsl.TestGroupMakers.*;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.*;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.UserInfo;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author sagyr
 * @since 8/7/13
 */

public class TrackableLaboratoryTest {

    public static final String FALLBACK_VALUE = "FALLBACK_VALUE";
    private CachedExperiments experiments;
    private static final UUID SOME_USER_GUID = UUID.fromString("19fc13d9-5943-4a87-82b1-4acb7e5cb039");
    private Maker<com.wixpress.petri.laboratory.UserInfo> aRegisteredUserInfo;

    private static final Class TheKey = new SpecDefinition() {
    }.getClass();

    private static final Class TheOtherKey = new SpecDefinition() {
    }.getClass();

    private final Maker<Experiment> experimentWithWinningFirstGroup = an(Experiment,
            with(id, 1),
            with(key, TheKey.getName()),
            with(testGroups, TEST_GROUPS_WITH_FIRST_ALWAYS_WINNING));

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    private TrackableLaboratory lab;
    private RamUserInfoStorage userInfoStorage;
    private InMemoryExperimentsSource cache;
    private FakeTestGroupAssignmentTracker testGroupAssignmentTracker;
    private FakeErrorHandler laboratoryErrorHandler;

    @Before
    public void setUp() throws Exception {
        cache = new InMemoryExperimentsSource();
        cache.write(new ArrayList<Experiment>());
        experiments = new CachedExperiments(cache);
        aRegisteredUserInfo = a(UserInfo,
                with(UserInfoMakers.userId, SOME_USER_GUID));
        userInfoStorage = new RamUserInfoStorage();
        testGroupAssignmentTracker = new FakeTestGroupAssignmentTracker();
        laboratoryErrorHandler = new FakeErrorHandler();
        lab = new TrackableLaboratory(experiments, testGroupAssignmentTracker, userInfoStorage,
                laboratoryErrorHandler);
    }

    public static class FakeErrorHandler implements ErrorHandler {

        private Throwable cause;

        @Override
        public void handle(String message, Throwable cause) {
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

    private void conductByKeyAndByScopeReturns(final String value) {
        assertThat(lab.conductExperiment(TheKey, ""), is(value));

        Map<String, String> expected = new HashMap<String, String>() {{
            put(TheKey.getName(), value);
        }};
        assertThat(lab.conductAllInScope("someScope"), is(expected));
    }

    private void errorReportWasSent(final Matcher<Throwable> exceptionMatcher) {
        assertThat(laboratoryErrorHandler.getCause(), is(exceptionMatcher));
    }

    private void writeUserInfoFromNullRequest() {
        writeUserInfoFromNullRequestOnHost("");
    }

    private void writeUserInfoFromNullRequestOnHost(String hostName) {
        UserInfo infoFromNullRequest = userInfoFromNullRequest(hostName);
        userInfoStorage.write(infoFromNullRequest);
    }

    private Matcher<Assignment> containsTheUsersId() {
        return new TypeSafeMatcher<Assignment>() {

            @Override
            protected boolean matchesSafely(Assignment assignment) {
                return assignment.getUserInfo().getUserId().equals(SOME_USER_GUID);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("an Assignment with user id " + SOME_USER_GUID);
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
    public void customTestGroupDrawerFromConductContextIsUsedWhenProvided() throws Exception {
        Experiment someExperiment = experimentWithWinningFirstGroup.but(
                with(testGroups, listOf(
                        a(TestGroup,
                                with(value, "123"),
                                with(groupId, 5),
                                with(probability, 100)),
                        a(TestGroup,
                                with(value, "321"),
                                with(groupId, 6),
                                with(probability, 0))
                ))
        ).make();

        addExperimentToCache(someExperiment);

        userInfoStorage.write(aRegisteredUserInfo.make());

        TestGroupDrawer customDrawer = mock(TestGroupDrawer.class);
        when(customDrawer.drawTestGroup(someExperiment)).thenReturn(someExperiment.getTestGroupById(6));

        ConductContext contextWithCustomKernel = ConductContextBuilder.newInstance().withTestGroupDrawer(customDrawer);

        assertThat(lab.conductExperiment(TheKey, -1, new IntegerConverter(), contextWithCustomKernel), is(321));

        verify(customDrawer).drawTestGroup(someExperiment);
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
                laboratoryErrorHandler);
        assertThat(lab.conductExperiment(TheKey, FALLBACK_VALUE), is(FALLBACK_VALUE));
        errorReportWasSent(Matchers.<Throwable>instanceOf(BlowingUpCachedExperiments.CacheExploded.class));
        assertBiLogIsEmpty();
    }

    @Test
    public void defaultValuesAreReturnedWhenUnexpectedExceptionIsThrown() {
        experiments = new BlowingUpCachedExperiments();
        userInfoStorage.write(aRegisteredUserInfo.make());
        lab = new TrackableLaboratory(experiments, new FakeTestGroupAssignmentTracker(), userInfoStorage,
                laboratoryErrorHandler);

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



    Matcher<Throwable> anException(final Class<?extends Throwable> type, final Matcher<String> msg) {
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
    public void defaultValuesAreReturnedWhenNoUserInfoExists() throws Exception {
        addExperimentToCache(experimentWithWinningFirstGroup.make());
        writeUserInfoFromNullRequest();

        final Matcher<Throwable> matcher = anException(UnsupportedOperationException.class, containsString("non-http flow"));
//                ofClass().withMessage(containsString("non-http flow")).matcher();

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
        final Experiment registeredUsersExperiment = experimentWithWinningFirstGroup.but(
                with(key, "for registered users"),
                with(onlyForLoggedIn, true)).make();

        addExperimentToCache(registeredUsersExperiment);

        userInfoStorage.write(aRegisteredUserInfo.make());
        lab.conductExperiment("for registered users", "", new StringConverter());

        userInfoStorage.assertUserExperimentsLog("1#1");
        userInfoStorage.assertAnonymousLogIs("");
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
    public void appendsBILogEntryButDoesNotPersistWhenExperimentIsNonPersistent() throws Exception {
        addExperimentToCache(experimentWithWinningFirstGroup.but(with(persistent, false)).make());
        userInfoStorage.write(aRegisteredUserInfo.make());
        lab.conductExperiment(TheKey, FALLBACK_VALUE);

        assertBiLogHasItemThat(allOf(
                containsTheUsersId()));
        assertAnonymousLogIsEmpty();
        assertUserLogIsEmpty();
    }


    @Test
    public void whenConductingSeveralExperimentsOnSameKeyFtsHavePrecedence() {
        Maker<com.wixpress.petri.experiments.domain.Experiment> experiment = experimentWithWinningFirstGroup.but(with(scope, "someScope"));
        Maker<com.wixpress.petri.experiments.domain.Experiment> ft = experiment.but(
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
    public void customTestGroupDrawerFromConductContextIsUsedEvenIfNullUserInfo() throws Exception {
        com.wixpress.petri.experiments.domain.Experiment experiment = experimentWithWinningFirstGroup.make();

        addExperimentToCache(experiment);
        writeUserInfoFromNullRequest();

        TestGroupDrawer customDrawer = mock(TestGroupDrawer.class);
        when(customDrawer.drawTestGroup(experiment)).thenReturn(experiment.getTestGroupById(2));
        ConductContext contextWithCustomKernel = ConductContextBuilder.newInstance().withTestGroupDrawer(customDrawer);

        assertThat(lab.conductExperiment(TheKey, FALLBACK_VALUE, contextWithCustomKernel), is(LOSING_VALUE));

        verify(customDrawer).drawTestGroup(experiment);
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


}
