package com.wixpress.petri.petri;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.natpryce.makeiteasy.Maker;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSnapshot;
import com.wixpress.petri.experiments.domain.Trigger;
import com.wixpress.petri.laboratory.dsl.ExperimentMakers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.an;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.*;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author: talyag
 * @since: 9/20/13
 */
public class JdbcExperimentsDaoIT extends PetriDaoIT<Experiment, ExperimentSnapshot> {

    private JdbcExperimentsDao updateableDao;
    private JdbcExperimentsDaoDriver daoDriver;

    private final String experimentKey = "ex1";
    private final Maker<Experiment> experiment = an(ExperimentMakers.Experiment, with(key, experimentKey), with(description, "desc"));
    private final ExperimentSnapshot snapshot = experiment.make().getExperimentSnapshot();

    @Before
    public void aSetup() {
        updateableDao = new JdbcExperimentsDao(dbDriver.jdbcTemplate, new ExperimentMapper(objectMapper, mappingErrorHandler));
        dao = updateableDao;
        daoDriver = new JdbcExperimentsDaoDriver(updateableDao, dbDriver, objectMapper);
    }

    @Test
    public void insert() throws JsonProcessingException {
        daoDriver.addExperimentWithMatchingSpec(snapshot);
        Experiment inserted = (Experiment) dao.fetch().get(0);
        assertThat(inserted.getId(), is(1));
        assertThat(inserted.getExperimentSnapshot(), is(snapshot));
        assertThat(dbDriver.jdbcTemplate.queryForObject("select count(*) from experiments where orig_id=1", Integer.class), is(1));
    }

    @Test(expected = FullPetriClient.UpdateFailed.class)
    public void throwsWhenUpdatingWithNonExistingId() throws JsonProcessingException {
        daoDriver.addExperimentWithMatchingSpec(snapshot);  //make sure key does exist
        int nonExisting = 3;

        updateableDao.update(experiment.but(with(id, nonExisting)).make(), new DateTime());
    }


    @Test(expected = FullPetriClient.CreateFailed.class)
    public void throwsWhenCreateExperimentWithNonExistingKey() {
        dao.add(snapshot);
    }

    @Test
    public void canCreateExperimentNotFromSpec() {
        dao.add(experiment.but(with(fromSpec, false)).make().getExperimentSnapshot());
    }

    @Test(expected = FullPetriClient.UpdateFailed.class)
    public void throwsWhenUpdateToNonExistingKey() throws JsonProcessingException {
        Experiment persistedExperiment = daoDriver.addExperimentWithMatchingSpec(snapshot);

        Experiment mutatedExperiment = this.experiment.but(
                with(key, "NON_EXISTING_KEY"),
                with(id, persistedExperiment.getId())).make();

        updateableDao.update(mutatedExperiment, new DateTime());
    }

    @Test
    public void canUpdateNotFromSpec() throws JsonProcessingException {
        Maker<Experiment> experimentSnapshotNotFromSpec = experiment.but(
                with(fromSpec, false), with(key, "NON_EXISTING_KEY"));
        Experiment persistedExperiment = dao.add(experimentSnapshotNotFromSpec.make().getExperimentSnapshot());

        Experiment mutatedExperiment = experimentSnapshotNotFromSpec.but(
                with(id, persistedExperiment.getId())).make();

        updateableDao.update(mutatedExperiment, new DateTime());
    }

    @Test
    public void update() throws JsonProcessingException {
        final DateTime now = new DateTime();
        Maker<Experiment> experimentWithExistingOriginalId = experiment.but(with(originalId, 1));
        Experiment persistedExperiment = daoDriver.addExperimentWithMatchingSpec(experimentWithExistingOriginalId.make().getExperimentSnapshot());

        Maker<Experiment> mutatedExperimentMaker = experimentWithExistingOriginalId.but(
                with(id, persistedExperiment.getId()),
                with(description, "bla"));
        Experiment mutatedExperiment = mutatedExperimentMaker.make();

        final DateTime updateTime = now.plusSeconds(1);
        updateableDao.update(mutatedExperiment, updateTime);

        Experiment expectedMutatedExperiment = mutatedExperimentMaker.but(with(lastUpdated, updateTime)).make();

        assertThat(updateableDao.fetch(), is(asList(expectedMutatedExperiment)));
        //assert orig id is propagated
        assertThat(dbDriver.jdbcTemplate.queryForObject("select count(*) from experiments where id=1 and orig_id=1", Integer.class), is(2));
        //TODO - perhaps replace with better assert once history feature is implemented
        assertThat(updateableDao.getHistoryById(persistedExperiment.getId()), is(asList(expectedMutatedExperiment, persistedExperiment)));

        //verify updated experiment can be terminated (bug found by chance only in E2E)
        updateableDao.update(expectedMutatedExperiment.terminateAsOf(new DateTime(), new Trigger("", "")), updateTime.plusSeconds(10));
    }


    @Test
    public void onlyLatestOriginalIdIsFetchedForDisplay() throws JsonProcessingException {
        Experiment persistedExperiment = daoDriver.addExperimentWithMatchingSpec(snapshot);

        Maker<Experiment> experimentWithSameOriginalId = this.experiment.but(
                with(originalId, persistedExperiment.getId()));
        updateableDao.add(experimentWithSameOriginalId.make().getExperimentSnapshot());

        List<Experiment> experiments = updateableDao.fetchAllExperimentsGroupedByOriginalId();

        assertThat(experiments.size(), is(1));
        assertThat(experiments.get(0).getId(), is(2));
        assertThat(experiments.get(0).getOriginalId(), is(1));
    }

    @Test
    public void historyContainsAllOfOriginalIds() throws JsonProcessingException {
        final DateTime now = new DateTime();
        Experiment experiment = daoDriver.addExperimentWithMatchingSpec(snapshot);

        Maker<Experiment> experimentWithSameOriginalId = this.experiment.but(
                with(originalId, experiment.getId()), with(creationDate, now.plusSeconds(5)));
        Experiment persistedWithCopiedOrigId = updateableDao.add(experimentWithSameOriginalId.make().getExperimentSnapshot());

        assertThat(updateableDao.getHistoryById(persistedWithCopiedOrigId.getId()), is(asList(persistedWithCopiedOrigId, experiment)));
    }

    @Test(expected = FullPetriClient.UpdateFailed.class)
    public void throwsWhenUpdatingStaleVersion() throws JsonProcessingException {
        Experiment persistedExperiment = daoDriver.addExperimentWithMatchingSpec(snapshot);

        DateTime now = new DateTime();
        updateableDao.update(persistedExperiment, now);
        updateableDao.update(persistedExperiment, now);
    }

    @Override
    protected void insertIllegalJson() {
        dbDriver.insertIllegalExperiment();
    }

    @Override
    protected ExperimentSnapshot objectToAdd() {
        return snapshot;
    }


}

