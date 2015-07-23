package com.wixpress.petri.petri;

import com.wixpress.petri.experiments.domain.ExperimentSpec;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder.anExperimentSpec;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class JdbcSpecsDaoIT extends PetriDaoIT<ExperimentSpec, ExperimentSpec> {

    private ExperimentSpec experimentSpec;
    private ExperimentSpec updatedSpec;
    private JdbcSpecsDao deleteEnablingDao;

    @Before
    public void aSetup() {
        deleteEnablingDao = new JdbcSpecsDao(dbDriver.jdbcTemplate, new SpecMapper(objectMapper, mappingErrorHandler));
        dao = deleteEnablingDao;

        experimentSpec = anExperimentSpec("f.q.n", new DateTime()).withTestGroups(asList("on", "off")).build();
        updatedSpec = anExperimentSpec("f.q.n", new DateTime()).withTestGroups(asList("a", "b", "c")).build();
    }


    @Test
    public void insert() {
        ExperimentSpec inserted = dao.add(experimentSpec);
        assertThat(inserted, is(experimentSpec));
        List<ExperimentSpec> fetched = dao.fetch();
        assertThat(fetched, is(asList(experimentSpec)));
    }

    @Test(expected = DuplicateKeyException.class)
    public void insertDuplicateClassNameThrowsException() {
        dao.add(experimentSpec);
        dao.add(experimentSpec);
    }

    @Test
    public void update() {
        dao.add(experimentSpec);
        dao.update(updatedSpec, new DateTime());
        assertThat(dao.fetch(), is(asList(updatedSpec)));
    }

    @Test
    public void delete() {
        dao.add(experimentSpec);
        deleteEnablingDao.delete(experimentSpec.getKey());
        assertThat(dao.fetch(), is(empty()));
    }


    @Override
    protected void insertIllegalJson() {
        dbDriver.insertSpec("illegalSpec", "IRRELEVANT");
    }

    @Override
    protected ExperimentSpec objectToAdd() {
        return experimentSpec;
    }


}
