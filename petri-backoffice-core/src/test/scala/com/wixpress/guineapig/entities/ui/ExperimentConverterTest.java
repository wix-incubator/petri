package com.wixpress.guineapig.entities.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.natpryce.makeiteasy.MakeItEasy;
import com.wixpress.guineapig.services.NoOpFilterAdapterExtender;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import com.wixpress.petri.laboratory.dsl.TestGroupMakers;
import com.wixpress.petri.petri.SpecDefinition;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static com.wixpress.petri.experiments.domain.ExperimentBuilder.anExperiment;
import static com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.anExperimentSnapshot;
import static com.wixpress.petri.experiments.domain.ScopeDefinition.aScopeDefinitionOnlyForLoggedInUsers;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.*;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class ExperimentConverterTest {

    protected static final String PUBLIC_SCOPE = "somePublicScope";
    private static final String OTHER_PUBLIC_SCOPE = "someOtherPublicScope";
    private static final String REGISTERED_SCOPE = "someRegisteredScope";

    private final ExperimentSnapshotBuilder activeSnapshotBuilder = anExperimentSnapshot().
            withGroups(TestGroupMakers.VALID_TEST_GROUP_LIST).withOnlyForLoggedInUsers(true);
    protected final List<ScopeDefinition> defaultHardCodedScopes = ImmutableList.of(new ScopeDefinition(PUBLIC_SCOPE, false));

    private ExperimentConverter experimentConverter =
            new ExperimentConverter(new AlwaysTrueIsEditablePredicate(), new NoOpFilterAdapterExtender());

    @Test
    public void convertsExperiment() throws ClassNotFoundException, IOException {
        Filter f = new RegisteredUsersFilter();
        Experiment expected = make(an(ExperimentMakers.Experiment
                        , with(creationDate, new DateTime())
                        , with(description, "some description")
                        , with(name, "experiment")
                        , with(fromSpec, true)
                        , with(id, 1)
                        , with(originalId, 1)
                        , with(linkedId, 7)
                        , with(filters, asList(f))
                        , with(key, "key")
                        , with(testGroups, TestGroupMakers.VALID_TEST_GROUP_LIST)
                        , MakeItEasy.with(scope, PUBLIC_SCOPE)
                        , with(creator, "name@wix.com")
                        , with(comment, "comment")
                        , with(updater, "someone@wix.com")
                        , with(featureToggle, false)
                        , with(paused, true)
                        , with(fromSpec, false)
                        , with(startDate, new DateTime())
                        , with(endDate, new DateTime()))
        );


        UiExperiment uiExperiment = experimentConverter.convert(expected);
        Experiment actual = UiExperimentConverter.toExperiment(uiExperiment, false, null, defaultHardCodedScopes, new NoOpFilterAdapterExtender(), "someone@wix.com");
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void activeExperimentGetsActiveState() throws JsonProcessingException, ClassNotFoundException {

        ExperimentSnapshot activeSnapshot = activeSnapshotBuilder.
                withStartDate(new DateTime()).
                withEndDate(new DateTime().plusYears(1)).
                build();
        assertStateIs(activeSnapshot, ExperimentState.ACTIVE);
    }

    @Test
    public void pausedExperimentGetsPausedState() throws JsonProcessingException, ClassNotFoundException {
        ExperimentSnapshot pausedExperiment = activeSnapshotBuilder.
                withStartDate(new DateTime()).
                withEndDate(new DateTime().plusYears(1)).
                withPaused(true).
                build();
        assertStateIs(pausedExperiment, ExperimentState.PAUSED);
    }

    @Test
    public void pausedEndedExperimentGetsEndedState() throws JsonProcessingException, ClassNotFoundException {
        ExperimentSnapshot pausedEndedExperiment = activeSnapshotBuilder.
                withStartDate(new DateTime().minusHours(2)).
                withEndDate(new DateTime().minusHours(1)).
                withPaused(true).
                build();
        assertStateIs(pausedEndedExperiment, ExperimentState.ENDED);
    }

    @Test
    public void pausedFutureExperimentGetsPausedState() throws JsonProcessingException, ClassNotFoundException {
        ExperimentSnapshot pausedFutureExperiment = activeSnapshotBuilder.
                withStartDate(new DateTime().plusHours(1)).
                withEndDate(new DateTime().plusYears(1)).
                withPaused(true).
                build();
        assertStateIs(pausedFutureExperiment, ExperimentState.PAUSED);
    }

    @Test
    public void endedExperimentGetsEndedState() throws JsonProcessingException, ClassNotFoundException {
        ExperimentSnapshot endedExperiment = activeSnapshotBuilder.
                withStartDate(new DateTime().minusHours(2)).
                withEndDate(new DateTime().minusHours(1)).
                build();
        assertStateIs(endedExperiment, ExperimentState.ENDED);
    }

    @Test
    public void futureExperimentGetsFutureState() throws JsonProcessingException, ClassNotFoundException {
        ExperimentSnapshot futureExperiment = activeSnapshotBuilder.
                withStartDate(new DateTime().plusHours(2)).
                withEndDate(new DateTime().plusYears(1)).
                build();
        assertStateIs(futureExperiment, ExperimentState.FUTURE);
    }

    private void assertStateIs(ExperimentSnapshot snapshot, ExperimentState state) throws JsonProcessingException, ClassNotFoundException {
        Experiment experiment = anExperiment().withExperimentSnapshot(snapshot).withLastUpdated(new DateTime()).build();
        UiExperiment uiExperiment = experimentConverter.convert(experiment);

        Assert.assertEquals(state.getState(), uiExperiment.getState());
    }

    @Test
    public void copiesValuesFromSpecWhenConverting() throws ClassNotFoundException, IOException {
        Experiment expectedNonPersistedExperiment = make(an(ExperimentMakers.Experiment
                , with(persistent, false)
                , with(allowedForBots, true)
                , with(ExperimentMakers.fromSpec, true)
                , MakeItEasy.with(scope, PUBLIC_SCOPE)
                , with(scopes, ImmutableList.of(PUBLIC_SCOPE))
                , with(updater, "")
        ));

        UiExperiment uiExperiment = experimentConverter.convert(expectedNonPersistedExperiment);

        final ExperimentSpec nonPersistentSpec = SpecDefinition.ExperimentSpecBuilder.aNewlyGeneratedExperimentSpec("key").withPersistent(false).withAllowedForBots(true).build();
        Experiment actual = UiExperimentConverter.toExperiment(uiExperiment, false, nonPersistentSpec, defaultHardCodedScopes, new NoOpFilterAdapterExtender(), "");

        Assert.assertEquals(expectedNonPersistedExperiment, actual);

    }

    @Test
    public void convertsExperimentWithMultipleScopes() throws Exception {

        Experiment experimentWithMultipleScopes = an(Experiment,
                with(scopes, ImmutableList.of(PUBLIC_SCOPE, OTHER_PUBLIC_SCOPE))
        ).make();

        UiExperiment converted = experimentConverter.convert(experimentWithMultipleScopes);
        assertEquals(experimentWithMultipleScopes.getScopes(),
                UiExperimentConverter.toExperiment(converted, false, null, defaultHardCodedScopes, new NoOpFilterAdapterExtender(), "").getScopes());
    }



    @Test
    public void convertsExperimentWithIncludeUserFilter() throws Exception {
        Filter includeUserIdsFilter = new IncludeUserIdsFilter(UUID.randomUUID());
        Experiment experimentWithIncludeUsers = an(Experiment,
                with(filters, asList(includeUserIdsFilter)),
                MakeItEasy.with(scope, PUBLIC_SCOPE)
        ).make();
        UiExperiment converted = experimentConverter.convert(experimentWithIncludeUsers);
        assertEquals(UiExperimentConverter.toExperiment(converted, false, null, defaultHardCodedScopes, new NoOpFilterAdapterExtender(), ""), experimentWithIncludeUsers);
    }

    @Test
    public void convertsExperimentWithConductionLimit() throws Exception {
        Experiment experimentWithConductionLimit = an(Experiment,
                with(conductionLimit, 2),
                MakeItEasy.with(scope, PUBLIC_SCOPE)
        ).make();
        UiExperiment converted = experimentConverter.convert(experimentWithConductionLimit);
        assertEquals(UiExperimentConverter.toExperiment(converted, false, null, defaultHardCodedScopes, new NoOpFilterAdapterExtender(), ""), experimentWithConductionLimit);
    }

    @Test
    public void convertsExperimentWithUserAgentRegexFilter() throws Exception {
        String androidRegex = "(.*)Android(.*)";
        String chromeRegex = "(.*)Chrome(.*)";
        Filter userAgentRegexFilter = new UserAgentRegexFilter(ImmutableList.of(androidRegex), ImmutableList.of(chromeRegex));
        Experiment experimentWithUserAgentRegexFilter = an(Experiment,
                with(filters, asList(userAgentRegexFilter)),
                MakeItEasy.with(scope, PUBLIC_SCOPE)
        ).make();
        UiExperiment converted = experimentConverter.convert(experimentWithUserAgentRegexFilter);
        assertEquals(UiExperimentConverter.toExperiment(converted, false, null, defaultHardCodedScopes, new NoOpFilterAdapterExtender(), ""), experimentWithUserAgentRegexFilter);

    }

    @Test
    public void convertsExperimentWithExcludeUserFilter() throws Exception {
        Filter excludeUserIdsFilter = new NotFilter(new IncludeUserIdsFilter(UUID.randomUUID()));
        Experiment experimentWithExcludeUsers = an(Experiment,
                with(filters, asList(excludeUserIdsFilter)),
                MakeItEasy.with(scope, PUBLIC_SCOPE)
        ).make();
        UiExperiment converted = experimentConverter.convert(experimentWithExcludeUsers);
        assertEquals(UiExperimentConverter.toExperiment(converted, false, null, defaultHardCodedScopes, new NoOpFilterAdapterExtender(), ""), experimentWithExcludeUsers);
    }

    @Test
    public void onlyOnRegisteredIsReadFromSpecWhenConverting() throws ClassNotFoundException, IOException {
        Experiment expectedExperiment = make(an(ExperimentMakers.Experiment
                , with(onlyForLoggedIn, true)
                , with(scope, "aScope")
        ));

        UiExperiment uiExperiment = experimentConverter.convert(expectedExperiment);

        final ExperimentSpec onlyOnRegisteredSpec = SpecDefinition.ExperimentSpecBuilder.aNewlyGeneratedExperimentSpec("key").
                withScopes(aScopeDefinitionOnlyForLoggedInUsers("aScope")).
                build();
        Experiment actual = UiExperimentConverter.toExperiment(uiExperiment, false, onlyOnRegisteredSpec, defaultHardCodedScopes, new NoOpFilterAdapterExtender(), "");

        Assert.assertEquals(expectedExperiment, actual);
    }

    @Test
    public void onlyOnRegisteredIsReadFromHardcodedScopesWhenConverting() throws ClassNotFoundException, IOException {
        Experiment expectedExperiment = make(an(ExperimentMakers.Experiment
                , with(onlyForLoggedIn, true)
                , MakeItEasy.with(scope, REGISTERED_SCOPE)
        ));

        UiExperiment uiExperiment = experimentConverter.convert(expectedExperiment);
        Experiment actual = UiExperimentConverter.toExperiment(uiExperiment, false, null, ImmutableList.of(new ScopeDefinition(REGISTERED_SCOPE, true)), new NoOpFilterAdapterExtender(), "");
        Assert.assertEquals(expectedExperiment, actual);
    }

    @Test
    public void nonRegisteredIsReadFromNonRegisteredUsersFilter() throws ClassNotFoundException, IOException {
        Experiment expectedExperiment = make(an(ExperimentMakers.Experiment
               , with(filters, ImmutableList.<Filter>of(new NonRegisteredUsersFilter()))
        ));

        UiExperiment uiExperiment = experimentConverter.convert(expectedExperiment);

        assertThat(uiExperiment.isNonRegistered(), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void exceptionIsThrownIfNoAvailableScopeWhenConverting() throws ClassNotFoundException, IOException {
        Experiment expectedExperiment = make(an(ExperimentMakers.Experiment
                , with(scope, "crazyScope")
        ));

        UiExperiment uiExperiment = experimentConverter.convert(expectedExperiment);
        UiExperimentConverter.toExperiment(uiExperiment, false, null, defaultHardCodedScopes, new NoOpFilterAdapterExtender(), "");
    }

    @Test
    public void convertsExperimentWitUserInGroupFilter() throws Exception {
        String  groupName = "myGroup";
        Filter userInGroupFilter = new UserNotInAnyGroupFilter(ImmutableList.of(groupName));
        Experiment experimentWithUserInGroupFilter = an(Experiment,
                with(filters, asList(userInGroupFilter)),
                MakeItEasy.with(scope, PUBLIC_SCOPE)
        ).make();
        UiExperiment converted = experimentConverter.convert(experimentWithUserInGroupFilter);
        assertEquals(UiExperimentConverter.toExperiment(converted, false, null, defaultHardCodedScopes, new NoOpFilterAdapterExtender(), ""), experimentWithUserInGroupFilter);

    }

}
