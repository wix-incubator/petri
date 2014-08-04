package com.wixpress.petri.experiments.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.natpryce.makeiteasy.Maker;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.laboratory.UserInfo;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static com.wixpress.petri.experiments.domain.Experiment.InvalidExperiment;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.Experiment;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.*;
import static com.wixpress.petri.laboratory.dsl.TestGroupMakers.TestGroup;
import static com.wixpress.petri.laboratory.dsl.TestGroupMakers.*;
import static com.wixpress.petri.laboratory.dsl.UserInfoMakers.*;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;


/**
 * @author sagyr
 * @since 9/16/13
 */
public class ExperimentTest {

    private static final DateTime NOW = new DateTime();
    private static final UUID SOME_USER_GUID = UUID.fromString("19fc13d9-5943-4a87-82b1-4acb7e5cb039");
    private static final UUID ANOTHER_USER_GUID = UUID.fromString("19fc13d9-5943-4a87-82b1-4acb7e5cb038");

    private void experimentIsNotActive(Experiment experimentWithUnknownEndDate) {
        assertFalse("Experiment should not be active", experimentWithUnknownEndDate.isActiveAt(NOW));
    }

    private void assertExperimentIsActive(Experiment experimentWithUnknownStartDate) {
        assertTrue("Experiment should be active", experimentWithUnknownStartDate.isActiveAt(NOW));
    }

    @Test
    public void canBeSerialized() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        Experiment theExperiment = an(Experiment).make();
        String json = objectMapper.writeValueAsString(theExperiment);
        Experiment deSerialized = objectMapper.readValue(json, new TypeReference<Experiment>() {
        });
        assertThat(deSerialized, is(theExperiment));
    }

    @Test
    public void canBeDeserializedFromNew() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        String jsonWithUnknownField = "{\"unknown\":0,\"id\":0,\"lastUpdated\":\"2013-12-04T15:51:38.591+02:00\",\"experimentSnapshot\":{\"key\":\"\",\"creationDate\":\"2013-12-04T15:51:38.591+02:00\",\"description\":\"\",\"startDate\":\"2013-12-04T15:51:38.565+02:00\",\"endDate\":\"2014-12-04T15:51:38.565+02:00\",\"groups\":[{\"id\":1,\"chunk\":50,\"value\":\"\"},{\"id\":2,\"chunk\":50,\"value\":\"\"}],\"scopes\":[],\"filters\":[],\"onlyForLoggedInUsers\":\"true\"}}";
        Experiment deSerialized = objectMapper.readValue(jsonWithUnknownField, new TypeReference<Experiment>() {
        });
        assertThat(deSerialized, is(notNullValue()));
    }


    @Test
    public void canBeDeserializedFromOld() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        String jsonWithNoPausedFieldAndTestGroupWithUnknownFields = "{\"id\":0,\"lastUpdated\":\"2013-12-04T15:51:38.591+02:00\",\"experimentSnapshot\":{\"key\":\"\",\"creationDate\":\"2013-12-04T15:51:38.591+02:00\",\"description\":\"\",\"startDate\":\"2013-12-04T15:51:38.565+02:00\",\"endDate\":\"2014-12-04T15:51:38.565+02:00\",\"groups\":[{\"id\":1,\"chunk\":50,\"value\":\"\",\"default\":false,\"lastUpdated\":null},{\"id\":2,\"chunk\":50,\"value\":\"\"}],\"scopes\":[],\"filters\":[],\"onlyForLoggedInUsers\":\"true\"}}";
        Experiment deSerialized = objectMapper.readValue(jsonWithNoPausedFieldAndTestGroupWithUnknownFields, new TypeReference<Experiment>() {
        });
        assertThat(deSerialized, is(notNullValue()));
        assertThat(deSerialized.getExperimentSnapshot().isPaused(), is(false));
    }

    @Test
    public void canBeSerializedWithFilters() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        Experiment theExperiment = an(Experiment, with(filters,
                asList(new FirstTimeVisitorsOnlyFilter(),
                        new GeoFilter(asList("us", "gb")),
                        new LanguageFilter(asList("en")),
                        new RegisteredUsersFilter(),
                        new NewUsersFilter(),
                        new WixEmployeesFilter(),
                        new HostFilter(asList("host1")))
        )).make();
        String json = objectMapper.writeValueAsString(theExperiment);
        Experiment deSerialized = objectMapper.readValue(json, new TypeReference<Experiment>() {
        });
        assertThat(deSerialized, is(theExperiment));
    }

    @Test(expected = InvalidExperiment.class)
    public void deserializedWithUnknownFilterIsNeverEligible() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        String jsonWithUnknownFilter = "{\"id\":0,\"lastUpdated\":\"2014-02-03T15:13:37.505+02:00\",\"experimentSnapshot\":{\"key\":\"\",\"creationDate\":\"2014-02-03T15:13:37.505+02:00\",\"description\":\"\",\"name\":\"\",\"startDate\":\"2014-02-03T15:13:37.504+02:00\",\"endDate\":\"2015-02-03T15:13:37.504+02:00\",\"paused\":false,\"groups\":[{\"id\":1,\"chunk\":50,\"value\":\"\",\"default\":false,\"lastUpdated\":null},{\"id\":2,\"chunk\":50,\"value\":\"\",\"default\":false,\"lastUpdated\":null}],\"scope\":\"\",\"filters\":[{\"filter-type\":\"anonymous\"},{\"filter-type\":\"geoo\"}],\"creator\":\"\",\"seed\":0,\"featureToggle\":false,\"onlyForLoggedInUsers\":\"true\"}}";
        Experiment deSerialized = objectMapper.readValue(jsonWithUnknownFilter, new TypeReference<Experiment>() {
        });

        deSerialized.isEligible(null);
    }

    @Test
    public void canBeEligibleForAnonymousUsersOnly() {
        List<Filter> anonFilter = new ArrayList<Filter>();
        anonFilter.add(new FirstTimeVisitorsOnlyFilter());
        Experiment experiment = an(Experiment, with(filters, anonFilter)).make();
        UserInfo registeredUserInfo = a(UserInfo).make();
        UserInfo anonymousUserInfo = AnonymousUserInfo.make();
        assertThat(experiment.isEligible(anonymousUserInfo), is(true));
        assertThat(experiment.isEligible(registeredUserInfo), is(false));
    }

    @Test
    public void returnsTestGroupsByChunk() {

        List<com.wixpress.petri.experiments.domain.TestGroup> groups = asList(
                a(TestGroup,
                        with(groupId, 0),
                        with(probability, 10)).
                        make(),
                a(TestGroup,
                        with(groupId, 1),
                        with(probability, 20)).
                        make(),
                a(TestGroup,
                        with(groupId, 2),
                        with(probability, 70)).
                        make()
        );

        Experiment experiment = an(Experiment,
                with(testGroups, groups)).make();
        assertThat(experiment.getTestGroupByChunk(0), is(groups.get(0)));
        assertThat(experiment.getTestGroupByChunk(9), is(groups.get(0)));
        assertThat(experiment.getTestGroupByChunk(10), is(groups.get(1)));
        assertThat(experiment.getTestGroupByChunk(29), is(groups.get(1)));
        assertThat(experiment.getTestGroupByChunk(30), is(groups.get(2)));
        assertThat(experiment.getTestGroupByChunk(100), is(groups.get(2)));
    }

    @Test
    public void experimentIsActiveWhenTimeSpanContainsDate() {
        Experiment experimentWithUnknownStartDate = an(Experiment,
                with(startDate, NOW.minusHours(1)),
                with(endDate, NOW.plusDays(1))).make();
        assertExperimentIsActive(experimentWithUnknownStartDate);
    }


    @Test
    public void experimentIsNotActiveWhenStartDateIsAfterDate() {
        Experiment experimentWithUnknownEndDate = an(Experiment,
                with(startDate, NOW.plusDays(1)),
                with(endDate, NOW.plusDays(2))).make();
        experimentIsNotActive(experimentWithUnknownEndDate);
    }

    @Test
    public void experimentIsNotActiveWhenEndDateIsBeforeDate() {
        Experiment experimentWithUnknownEndDate = an(Experiment,
                with(startDate, NOW.minusDays(2)),
                with(endDate, NOW.minusDays(1))).make();
        experimentIsNotActive(experimentWithUnknownEndDate);
    }

    @Test
    public void experimentWithFutureEndDateIsNotTerminated() {
        Experiment experimentWithFutureEndDate = an(Experiment,
                with(startDate, NOW.minusDays(1)),
                with(endDate, NOW.plusDays(1))).make();
        assertThat(experimentWithFutureEndDate.isTerminated(), is(false));
    }

    @Test
    public void experimentWithPastEndDateIsTerminated() {
        Experiment experimentWithPastEndDate = an(Experiment,
                with(startDate, NOW.minusDays(1)),
                with(endDate, NOW.minusHours(6))).make();
        assertThat(experimentWithPastEndDate.isTerminated(), is(true));
    }

    @Test
    public void experimentWithFutureEndDateButEqualsToStartDateIsTerminated() {
        Experiment cancelledExperiment = an(Experiment,
                with(startDate, NOW.plusDays(1)),
                with(endDate, NOW.plusDays(1))).make();
        assertThat(cancelledExperiment.isTerminated(), is(true));
    }

    @Test
    public void terminateActiveExperiment() {
        Experiment activeExperiment = an(Experiment,
                with(startDate, NOW.minusDays(1)),
                with(endDate, NOW.plusDays(1))).make();
        Experiment terminated = activeExperiment.terminateAsOf(NOW,"terminate active experiment");
        assertThat(terminated.getEndDate(), is(NOW));
    }

    @Test
    public void terminateFutureExperiment() {
        Experiment futureExperiment = an(Experiment,
                with(startDate, NOW.plusDays(1)),
                with(endDate, NOW.plusDays(2))).make();
        Experiment terminated = futureExperiment.terminateAsOf(NOW,"terminate future experiment");
        assertThat(terminated.getEndDate(), is(futureExperiment.getStartDate()));
    }

    private Experiment experimentWithFilters(Filter... filter) {
        return an(Experiment, with(ExperimentMakers.filters, asList(filter))).make();
    }

    private void userIsEligible(Experiment experiment, UserInfo user) {
        assertThat(experiment.isEligible(user), is(true));
    }

    private void assertUserIsNotEligible(Experiment experiment, UserInfo user) {
        assertThat(experiment.isEligible(user), is(false));
    }

    @Test
    public void eligibilityForWixEmployeesOrAnonymous() {
        Experiment experiment = experimentWithFilters(new WixEmployeesFilter(), new FirstTimeVisitorsOnlyFilter());

        UserInfo aWixUserInfo = a(UserInfo, with(email, "a@wix.com")).make();
        userIsEligible(experiment, aWixUserInfo);

        userIsEligible(experiment, AnonymousUserInfo.make());

        UserInfo aRegisteredUserInfo = a(UserInfo, with(userId, SOME_USER_GUID)).make();
        assertUserIsNotEligible(experiment, aRegisteredUserInfo);

    }

    @Test
    public void eligibilityForWixEmployeesOnly() {
        Experiment experiment = experimentWithFilters(new WixEmployeesFilter());

        UserInfo aNonWixUserInfo = a(UserInfo, with(email, "a@not-wix.com")).make();
        assertUserIsNotEligible(experiment, aNonWixUserInfo);

        UserInfo aWixUserInfo = a(UserInfo, with(email, "a@wix.com")).make();
        userIsEligible(experiment, aWixUserInfo);
    }

    @Test
    public void eligibilityForWixEmployeesExcludingOne() {
        Experiment experiment = experimentWithFilters(
                new WixEmployeesFilter(), new NotFilter(new IncludeUserIdsFilter(SOME_USER_GUID))
        );

        UserInfo excludedWixUserInfo = a(UserInfo, with(email, "a@wix.com"), with(userId, SOME_USER_GUID)).make();
        assertUserIsNotEligible(experiment, excludedWixUserInfo);

        UserInfo otherWixUserInfo = a(UserInfo, with(email, "b@wix.com"), with(userId, ANOTHER_USER_GUID)).make();
        userIsEligible(experiment, otherWixUserInfo);

        UserInfo otherNonWixUserInfo = a(UserInfo, with(email, "b@non-wix.com"), with(userId, ANOTHER_USER_GUID)).make();
        assertUserIsNotEligible(experiment, otherNonWixUserInfo);
    }

    @Test
    public void eligibilityForExcludeUidOnly() {
        Experiment experimentExcludingOneUser = experimentWithFilters(
                new NotFilter(new IncludeUserIdsFilter(SOME_USER_GUID)));

        UserInfo excludedUserInfo = a(UserInfo, with(userId, SOME_USER_GUID)).make();
        assertUserIsNotEligible(experimentExcludingOneUser, excludedUserInfo);

        UserInfo nonExcludedUserInfo = a(UserInfo, with(userId, ANOTHER_USER_GUID)).make();
        userIsEligible(experimentExcludingOneUser, nonExcludedUserInfo);
    }


    @Test
    public void eligibilityForWixEmployeesAndIncludedUids() {
        Experiment experiment = experimentWithFilters(
                new WixEmployeesFilter(), new IncludeUserIdsFilter(SOME_USER_GUID)
        );

        UserInfo aWixUserInfo = a(UserInfo, with(email, "a@wix.com")).make();
        userIsEligible(experiment, aWixUserInfo);

        UserInfo includedNonWixUserInfo = a(UserInfo, with(email, "b@not-wix.com"), with(userId, SOME_USER_GUID)).make();
        userIsEligible(experiment, includedNonWixUserInfo);

        UserInfo nonIncludedNonWixUserInfo = a(UserInfo, with(email, "b@not-wix.com"), with(userId, ANOTHER_USER_GUID)).make();
        assertUserIsNotEligible(experiment, nonIncludedNonWixUserInfo);
    }

    @Test
    public void eligibilityForIncludeUidOnly() {
        Experiment experimentIncludingOnlyOne = experimentWithFilters(new IncludeUserIdsFilter(SOME_USER_GUID));

        UserInfo nonIncludedUserInfo = a(UserInfo, with(userId, ANOTHER_USER_GUID)).make();
        assertUserIsNotEligible(experimentIncludingOnlyOne, nonIncludedUserInfo);

        UserInfo userInfoWithNoUid = AnonymousUserInfo.make();
        assertUserIsNotEligible(experimentIncludingOnlyOne, userInfoWithNoUid);

        UserInfo includedUserInfo = a(UserInfo, with(userId, SOME_USER_GUID)).make();
        userIsEligible(experimentIncludingOnlyOne, includedUserInfo);
    }

    @Test
    public void isEligibleForMultipleInclusiveFilters() {
        Experiment experimentWithGeoAndLangFilters = experimentWithFilters(
                new GeoFilter(asList("fr")), new LanguageFilter(asList("en"))
        );

        UserInfo wrongLanguageUserInfo = a(UserInfo, with(language, "not-en"), with(country, "fr")).make();
        assertUserIsNotEligible(experimentWithGeoAndLangFilters, wrongLanguageUserInfo);

        UserInfo wrongCountryUserInfo = a(UserInfo, with(language, "en"), with(country, "non-fr")).make();
        assertUserIsNotEligible(experimentWithGeoAndLangFilters, wrongCountryUserInfo);

        UserInfo includedUserInfo = a(UserInfo, with(language, "en"), with(country, "fr")).make();
        userIsEligible(experimentWithGeoAndLangFilters, includedUserInfo);
    }

    @Test
    public void toOpenToAllAlwaysReturningNew() {
        final Maker<Experiment> abTestWithFilters = an(Experiment,
                with(testGroups, asList(new TestGroup(7, 50, "old"), new TestGroup(12, 50, "new"))),
                with(filters, new ArrayList<Filter>() {{
                    add(new RegisteredUsersFilter());
                }})
        );
        Experiment updated = abTestWithFilters.make().toOpenToAllTestGroupValueAlwaysReturning("new");

        assertThat(updated.getFilters(), is(empty()));
        assertThat(updated.isToggle(), is(true));
        assertThat(updated.getGroups(), is(asList(new TestGroup(7, 0, "old"), new TestGroup(12, 100, "new"))));
    }


    @Test
    public void canBeSerializedWithForRegisteredOnly() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        Experiment theExperiment = an(Experiment, with(onlyForLoggedIn, true)).make();
        String json = objectMapper.writeValueAsString(theExperiment);
        Experiment deSerialized = objectMapper.readValue(json, new TypeReference<Experiment>() {
        });
        assertThat(deSerialized, is(theExperiment));
    }

    @Test
    public void canBeSerializedWithCommentAndEditor() throws IOException {
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        Experiment theExperiment = an(Experiment, with(comment, "some very good reason"), with(editor, "someone@wix.com")).make();
        String json = objectMapper.writeValueAsString(theExperiment);
        Experiment deSerialized = objectMapper.readValue(json, new TypeReference<Experiment>() {
        });
        assertThat(deSerialized, is(theExperiment));
    }

}
