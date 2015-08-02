package com.wixpress.petri.petri;


import com.google.common.collect.ImmutableList;
import com.natpryce.makeiteasy.Maker;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import com.wixpress.petri.laboratory.dsl.TestGroupMakers;
import com.wixpress.petri.util.ConductExperimentSummaryMatcher;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.natpryce.makeiteasy.MakeItEasy.an;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.experiments.domain.ExperimentBuilder.aCopyOf;
import static com.wixpress.petri.experiments.domain.ExperimentBuilder.anExperiment;
import static com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.anExperimentSnapshot;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.id;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.key;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.anExperimentSpec;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public abstract class PetriClientContractTest {


    private final Maker<Experiment> aValidExperiment = an(ExperimentMakers.Experiment,
            with(ExperimentMakers.testGroups, TestGroupMakers.VALID_TEST_GROUP_LIST));

    protected final ExperimentSnapshotBuilder anActiveSnapshot = anExperimentSnapshot().withGroups(TestGroupMakers.VALID_TEST_GROUP_LIST).
            withStartDate(ExperimentMakers.DEFAULT_START_DATE).
            withEndDate(ExperimentMakers.DEFAULT_END_DATE).
            withCreationDate(ExperimentMakers.DEFAULT_CREATION_DATE).
            withOnlyForLoggedInUsers(true);

    private final ExperimentSnapshotBuilder inactiveSnapshot = anExperimentSnapshot().withGroups(TestGroupMakers.VALID_TEST_GROUP_LIST).
            withStartDate(ExperimentMakers.DEFAULT_END_DATE).
            withEndDate(ExperimentMakers.DEFAULT_END_DATE).
            withCreationDate(ExperimentMakers.DEFAULT_CREATION_DATE).
            withOnlyForLoggedInUsers(true);

    private final ExperimentSnapshotBuilder anActiveSnapshotWithKey = anActiveSnapshot.withKey("ex1");

    //TODO - remove duplication with GP IT
    protected Experiment addExperimentWithKey(ExperimentSnapshotBuilder snapshotBuilder) {
        ExperimentSnapshot snapshot = snapshotBuilder.build();

        SpecDefinition.ExperimentSpecBuilder builder =
                new SpecDefinition.ExperimentSpecBuilder(snapshot.key(), new DateTime());
        fullPetriClient().addSpecs(asList(builder.build()));

        return fullPetriClient().insertExperiment(snapshot);
    }

    private Experiment updateDescription(Experiment experiment, ExperimentSnapshotBuilder aSnapshot) {
        Experiment experimentToUpdate =
                aCopyOf(experiment).
                        withExperimentSnapshot(aSnapshot.
                                withDescription("UPDATED").
                                build()).
                        build();

        return fullPetriClient().updateExperiment(experimentToUpdate);
    }

    protected abstract FullPetriClient fullPetriClient();

    protected abstract PetriClient petriClient();

    protected abstract UserRequestPetriClient synchPetriClient();


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void returnsEmptyListWhenNoneDefined() {
        assertThat(fullPetriClient().fetchAllExperiments(), is(empty()));
    }

    @Test
    public void createsAndRetrievesSingleExperiment() {
        Experiment experiment = addExperimentWithKey(anActiveSnapshotWithKey);
        final List<Experiment> actual = fullPetriClient().fetchAllExperiments();
        assertThat(actual, is(asList(experiment)));
    }

    @Test
    public void createsAndRetrievesTwoExperiments() {
        Experiment experiment1 = addExperimentWithKey(anActiveSnapshot.withKey("ex1"));
        Experiment experiment2 = addExperimentWithKey(anActiveSnapshot.withKey("ex2"));
        assertThat(fullPetriClient().fetchAllExperiments(), is(asList(experiment1, experiment2)));
    }

    @Test
    public void fetchActiveRetrievesOnlyActive() {
        Experiment active = addExperimentWithKey(anActiveSnapshotWithKey);
        addExperimentWithKey(inactiveSnapshot.withKey("ex1"));

        assertThat(petriClient().fetchActiveExperiments(), is(asList(active)));
    }

    @Test
    public void fetchAllRetrievesLastVersionOfExperiment() {
        Experiment experiment = addExperimentWithKey(anActiveSnapshotWithKey);
        Experiment updated = updateDescription(experiment, anActiveSnapshot);

        assertThat(fullPetriClient().fetchAllExperiments(), is(asList(updated)));
    }

    @Test
    public void fetchActiveRetrievesLastVersionOfExperiment() {
        Experiment experiment = addExperimentWithKey(anActiveSnapshotWithKey);
        Experiment updated = updateDescription(experiment, anActiveSnapshot);

        assertThat(petriClient().fetchActiveExperiments(), is(asList(updated)));
    }

    @Test
    public void getHistoryContainsUpdatedAndOriginal() {
        Experiment experiment = addExperimentWithKey(anActiveSnapshotWithKey);
        Experiment updatedExperiment = updateDescription(experiment, anActiveSnapshot);

        List<Experiment> historyById = fullPetriClient().getHistoryById(experiment.getId());
        assertThat(historyById, is(asList(updatedExperiment, experiment)));
    }

    @Test
    public void createExperimentWithNonExistingKeyThrowsException() {
        String nonExistingKey = "someKey";

        thrown.expect(FullPetriClient.CreateFailed.class);
        thrown.expectMessage(allOf(containsString("unable to add a"), containsString("ExperimentSnapshot"),
                containsString("with key 'someKey")));

        fullPetriClient().insertExperiment(anActiveSnapshot.withKey(nonExistingKey).build());
    }

    @Test
    public void updateExperimentToNonExistingKeyThrowsException() {
        Experiment experiment = addExperimentWithKey(anActiveSnapshot.withKey("ex1"));
        Experiment mutatedExperiment = aValidExperiment.but(with(key, "ex2"), with(id, experiment.getId())).make();

        thrown.expect(FullPetriClient.UpdateFailed.class);
        thrown.expectMessage("Failed to update experiment 1");

        fullPetriClient().updateExperiment(mutatedExperiment);
    }

    @Test(expected = FullPetriClient.UpdateFailed.class)
    public void updateExperimentWithNonExistingIdThrowsException() {
        SpecDefinition.ExperimentSpecBuilder builder =
                new SpecDefinition.ExperimentSpecBuilder("ex1", new DateTime()).withTestGroups(asList("1", "2"));
        fullPetriClient().addSpecs(asList(builder.build()));
        int nonExistingId = 3;
        fullPetriClient().updateExperiment(aValidExperiment.but(with(key, "ex1"), with(id, nonExistingId)).make());
    }

    @Test
    public void createsAndRetrievesSingleSpec() {
        ExperimentSpec experimentSpec = anExperimentSpec("f.q.n.Spec1", new DateTime()).withTestGroups(asList("yellow", "green")).build();

        fullPetriClient().addSpecs(asList(experimentSpec));

        assertThat(fullPetriClient().fetchSpecs(), is(asList(experimentSpec)));
    }

    @Test
    public void createsAndRetrievesExperimentWithFilters() {
        SpecDefinition.ExperimentSpecBuilder builder =
                new SpecDefinition.ExperimentSpecBuilder("ex1", new DateTime()).withTestGroups(asList("1", "2"));
        fullPetriClient().addSpecs(asList(builder.build()));
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new FirstTimeVisitorsOnlyFilter());
        Experiment experiment = fullPetriClient().insertExperiment(
                anActiveSnapshot.withKey("ex1").withFilters(filters).build());

        Experiment expected = anExperiment().withId(1).withLastUpdated(experiment.getCreationDate()).withExperimentSnapshot(experiment.getExperimentSnapshot()).build();
        assertThat(fullPetriClient().fetchAllExperiments(), is(asList(expected)));
    }

    @Test
    public void experimentNotFromSpecCanBeUpdatedTwice() {
        ExperimentSnapshot experimentNotFromSpec = anActiveSnapshot.withKey("ex1").withFromSpec(false).build();
        fullPetriClient().insertExperiment(experimentNotFromSpec);

        Experiment persistedExperiment = petriClient().fetchActiveExperiments().get(0);
        Experiment mutatedExperiment = aCopyOf(persistedExperiment).
                withExperimentSnapshot(ExperimentSnapshotBuilder.aCopyOf(persistedExperiment.getExperimentSnapshot()).withDescription("ha").build()).
                build();
        fullPetriClient().updateExperiment(mutatedExperiment);

        persistedExperiment = petriClient().fetchActiveExperiments().get(0);
        mutatedExperiment = aCopyOf(persistedExperiment).
                withExperimentSnapshot(ExperimentSnapshotBuilder.aCopyOf(persistedExperiment.getExperimentSnapshot()).withDescription("haha!").build()).
                build();
        fullPetriClient().updateExperiment(mutatedExperiment);
    }

    @Test
    public void specKeyIsCaseInsensitive() throws InterruptedException {
        ExperimentSpec spec = anExperimentSpec("allLowerCase", new DateTime()).withTestGroups(asList("yellow", "green")).build();
        ExperimentSpec duplicateSpec = anExperimentSpec("alllowercase", new DateTime()).withTestGroups(asList("yellow", "green")).build();

        fullPetriClient().addSpecs(asList(spec));
        fullPetriClient().addSpecs(asList(duplicateSpec));

        assertThat(fullPetriClient().fetchSpecs().size(), is(1));
        assertThat(fullPetriClient().fetchSpecs().get(0).getKey(), is("alllowercase"));
    }

    @Test
    public void deleteSpec() {
        ExperimentSpec experimentSpec = anExperimentSpec("talya", new DateTime()).build();
        fullPetriClient().addSpecs(asList(experimentSpec));

        fullPetriClient().deleteSpec(experimentSpec.getKey());
        assertThat(fullPetriClient().fetchSpecs(), is(empty()));
    }

    @Test
    public void conductExperimentIsReported() throws InterruptedException {

        int experimentId = 12;
        List<ConductExperimentReport> conductedExperiments = ImmutableList.of(new ConductExperimentReport("localhost", experimentId, "true", 3l));
        petriClient().reportConductExperiment(conductedExperiments);

        List<ConductExperimentSummary> experimentSummary = fullPetriClient().getExperimentReport(experimentId);
        assertThat(experimentSummary.size(), is(1));
        assertThat(experimentSummary, contains(ConductExperimentSummaryMatcher.hasSummary("localhost", experimentId, "true", 3l)));

    }

    @Test
    public void userTestGroupsAreSaved() {

        String cookieValue = "1#5";
        UUID userGuid = UUID.randomUUID();
        petriClient().saveUserState(userGuid, cookieValue);

        assertThat(synchPetriClient().getUserState(userGuid),is(cookieValue));
    }


}
