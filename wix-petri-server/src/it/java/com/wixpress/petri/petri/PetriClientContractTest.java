package com.wixpress.petri.petri;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.natpryce.makeiteasy.Maker;
import com.wixpress.petri.experiments.domain.*;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import com.wixpress.petri.laboratory.dsl.TestGroupMakers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.an;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.experiments.domain.ExperimentBuilder.aCopyOf;
import static com.wixpress.petri.experiments.domain.ExperimentBuilder.anExperiment;
import static com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.anExperimentSnapshot;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.id;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.key;
import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.anExperimentSpec;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
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
        petriClient().addSpecs(asList(builder.build()));

        return petriClient().insertExperiment(snapshot);
    }

    private Experiment updateDescription(Experiment experiment, ExperimentSnapshotBuilder aSnapshot) {
        Experiment experimentToUpdate =
                aCopyOf(experiment).
                        withExperimentSnapshot(aSnapshot.
                                withDescription("UPDATED").
                                build()).
                        build();

        return petriClient().updateExperiment(experimentToUpdate);
    }

    protected abstract PetriClient petriClient();

    @Test
    public void returnsEmptyListWhenNoneDefined() {
        assertThat(petriClient().fetchAllExperiments(), is(empty()));
    }

    @Test
    public void createsAndRetrievesSingleExperiment() {
        Experiment experiment = addExperimentWithKey(anActiveSnapshotWithKey);
        final List<Experiment> actual = petriClient().fetchAllExperiments();
        assertThat(actual, is(asList(experiment)));
    }

    @Test
    public void createsAndRetrievesTwoExperiments() {
        Experiment experiment1 = addExperimentWithKey(anActiveSnapshot.withKey("ex1"));
        Experiment experiment2 = addExperimentWithKey(anActiveSnapshot.withKey("ex2"));
        assertThat(petriClient().fetchAllExperiments(), is(asList(experiment1, experiment2)));
    }

    @Test
    public void fectActiveRetrievesOnlyActive() {
        Experiment active = addExperimentWithKey(anActiveSnapshotWithKey);
        addExperimentWithKey(inactiveSnapshot.withKey("ex1"));

        assertThat(petriClient().fetchActiveExperiments(), is(asList(active)));
    }

    @Test
    public void fetchAllRetrievesLastVersionOfExperiment() {
        Experiment experiment = addExperimentWithKey(anActiveSnapshotWithKey);
        Experiment updated = updateDescription(experiment, anActiveSnapshot);

        assertThat(petriClient().fetchAllExperiments(), is(asList(updated)));
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

        List<Experiment> historyById = petriClient().getHistoryById(experiment.getId());
        assertThat(historyById, is(asList(updatedExperiment, experiment)));
    }

    @Test(expected = PetriClient.CreateFailed.class)
    public void createExperimentWithNonExistingKeyThrowsException() {
        String nonExistingKey = "someKey";
        petriClient().insertExperiment(anActiveSnapshot.withKey(nonExistingKey).build());
    }

    @Test(expected = PetriClient.UpdateFailed.class)
    public void updateExperimentToNonExistingKeyThrowsException() {
        Experiment experiment = addExperimentWithKey(anActiveSnapshot.withKey("ex1"));
        Experiment mutatedExperiment = aValidExperiment.but(with(key, "ex2"), with(id, experiment.getId())).make();
        petriClient().updateExperiment(mutatedExperiment);
    }

    @Test(expected = PetriClient.UpdateFailed.class)
    public void updateExperimentWithNonExistingIdThrowsException() {
        SpecDefinition.ExperimentSpecBuilder builder =
                new SpecDefinition.ExperimentSpecBuilder("ex1", new DateTime()).withTestGroups(asList("1", "2"));
        petriClient().addSpecs(asList(builder.build()));
        int nonExistingId = 3;
        petriClient().updateExperiment(aValidExperiment.but(with(key, "ex1"), with(id, nonExistingId)).make());
    }

    @Test
    public void createsAndRetrievesSingleSpec() {
        ExperimentSpec experimentSpec = anExperimentSpec("f.q.n.Spec1", new DateTime()).withTestGroups(asList("yellow", "green")).build();

        petriClient().addSpecs(asList(experimentSpec));

        assertThat(petriClient().fetchSpecs(), is(asList(experimentSpec)));
    }

    @Test
    public void createsAndRetrievesExperimentWithFilters() {
        SpecDefinition.ExperimentSpecBuilder builder =
                new SpecDefinition.ExperimentSpecBuilder("ex1", new DateTime()).withTestGroups(asList("1", "2"));
        petriClient().addSpecs(asList(builder.build()));
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new FirstTimeVisitorsOnlyFilter());
        Experiment experiment = petriClient().insertExperiment(
                anActiveSnapshot.withKey("ex1").withFilters(filters).build());

        Experiment expected = anExperiment().withId(1).withLastUpdated(experiment.getCreationDate()).withExperimentSnapshot(experiment.getExperimentSnapshot()).build();
        assertThat(petriClient().fetchAllExperiments(), is(asList(expected)));
    }

    @Test
    public void experimentNotFromSpecCanBeUpdatedTwice() {
        ExperimentSnapshot experimentNotFromSpec = anActiveSnapshot.withKey("ex1").withFromSpec(false).build();
        petriClient().insertExperiment(experimentNotFromSpec);

        Experiment persistedExperiment = petriClient().fetchActiveExperiments().get(0);
        Experiment mutatedExperiment = ExperimentBuilder.aCopyOf(persistedExperiment).
                withExperimentSnapshot(ExperimentSnapshotBuilder.aCopyOf(persistedExperiment.getExperimentSnapshot()).withDescription("ha").build()).
                build();
        petriClient().updateExperiment(mutatedExperiment);

        persistedExperiment = petriClient().fetchActiveExperiments().get(0);
        mutatedExperiment = ExperimentBuilder.aCopyOf(persistedExperiment).
                withExperimentSnapshot(ExperimentSnapshotBuilder.aCopyOf(persistedExperiment.getExperimentSnapshot()).withDescription("haha!").build()).
                build();
        petriClient().updateExperiment(mutatedExperiment);
    }

    @Test
    public void specKeyIsCaseInsensitive() throws IOException {
        ExperimentSpec spec = anExperimentSpec("f.q.n.Spec1", new DateTime()).withTestGroups(asList("yellow", "green")).build();
        ExperimentSpec duplicateSpec = anExperimentSpec("f.q.n.spec1", new DateTime()).withTestGroups(asList("blue", "green")).build();

        petriClient().addSpecs(asList(spec));
        petriClient().addSpecs(asList(duplicateSpec));

        final List<ExperimentSpec> actualSpecs = petriClient().fetchSpecs();
        assertThat(actualSpecs, hasSize(1));
        final ExperimentSpec actualSpec = actualSpecs.get(0);
        assertThat(actualSpec.getKey(), is("f.q.n.spec1"));
        assertThat(actualSpec.getTestGroups(), is(asList("blue","green")));
    }

    @Test
    public void deleteSpec() {
        ExperimentSpec experimentSpec = anExperimentSpec("talya", new DateTime()).build();
        petriClient().addSpecs(asList(experimentSpec));

        petriClient().deleteSpec(experimentSpec.getKey());
        assertThat(petriClient().fetchSpecs(), is(empty()));
    }


}
