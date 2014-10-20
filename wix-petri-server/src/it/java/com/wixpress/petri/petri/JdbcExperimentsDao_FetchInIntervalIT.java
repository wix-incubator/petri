package com.wixpress.petri.petri;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.natpryce.makeiteasy.Maker;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import util.DBDriver;

import java.sql.SQLException;
import java.util.List;

import static com.natpryce.makeiteasy.MakeItEasy.a;
import static com.natpryce.makeiteasy.MakeItEasy.with;
import static com.wixpress.petri.laboratory.dsl.ExperimentMakers.*;
import static com.wixpress.petri.petri.PetriDaoIT.JDBC_H2_IN_MEM_CONNECTION_STRING;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author: talyag
 * @since: 9/20/13
 */
public class JdbcExperimentsDao_FetchInIntervalIT {

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();

    private JdbcExperimentsDao dao;
    private DBDriver dbDriver;

    private Experiment experiment;
    private DateTime now;

    private void generateSchema() throws SQLException, ClassNotFoundException {
        dbDriver = DBDriver.dbDriver(JDBC_H2_IN_MEM_CONNECTION_STRING);
        dbDriver.createSchema();
    }

    @Before
    public void setup() throws SQLException, ClassNotFoundException, JsonProcessingException {
        generateSchema();
        ObjectMapper objectMapper = ObjectMapperFactory.makeObjectMapper();
        MappingErrorHandler mappingErrorHandler = context.mock(MappingErrorHandler.class);

        dao = new JdbcExperimentsDao(dbDriver.jdbcTemplate, new ExperimentMapper(objectMapper, mappingErrorHandler));
        JdbcExperimentsDaoDriver daoDriver = new JdbcExperimentsDaoDriver(dao, dbDriver, objectMapper);

        addNewlyCreatedExperiment(daoDriver);
    }

    @After
    public void closeConnection() throws SQLException {
        dbDriver.closeConnection();
    }

    private void addNewlyCreatedExperiment(JdbcExperimentsDaoDriver daoDriver) throws JsonProcessingException {
        now = new DateTime();
        experiment = daoDriver.addExperimentWithMatchingSpec(a(
                Experiment,
                with(key, "ex1"),
                with(creationDate, now))
                .make()
                .getExperimentSnapshot());
    }

    private void updateExperimentInFutureMinutes(int minutes, Maker<Experiment> anExperiment) {
        Experiment latestVersion = anExperiment.make();
        DateTime updateTime = now.plusMinutes(minutes);
        dao.update(latestVersion, updateTime);
    }

    @Test
    public void doesNotReturnExperimentsNotInRange() throws JsonProcessingException {
        List<Experiment> experiments = dao.fetchInInterval(now.minusMinutes(3), now.minusMinutes(1));

        assertThat(experiments.size(), is(0));
    }

    @Test
    public void onlyLatestUpdateIsReturned() throws JsonProcessingException {
        updateExperimentInFutureMinutes(1, a(copyOf(experiment)).but(with(description, "updatedDesc")));

        List<Experiment> experiments = dao.fetchInInterval(now.minusMinutes(30), now.plusMinutes(30));

        assertThat(experiments.size(), is(1));
        assertThat(experiments.get(0).getDescription(), is("updatedDesc"));
    }

    @Test
    public void returnLastUpdateOfExperimentInGivenIntervalEvenIfMoreRecentUpdatesExist() throws JsonProcessingException {
        updateExperimentInFutureMinutes(35, a(copyOf(experiment)).but(with(description, "updatedDesc")));

        List<Experiment> experiments = dao.fetchInInterval(now.minusMinutes(30), now.plusMinutes(30));

        assertThat(experiments, is(asList(experiment)));
    }


}

