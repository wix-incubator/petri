package com.wixpress.petri.petri;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import util.DBDriver;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public abstract class PetriDaoIT<T, V> {
    public static final String JDBC_H2_IN_MEM_CONNECTION_STRING = "jdbc:h2:mem:test;MODE=MySQL";

    @Rule
    public JUnitRuleMockery context = new JUnitRuleMockery();
    protected DBDriver dbDriver;
    protected MappingErrorHandler mappingErrorHandler;
    protected ObjectMapper objectMapper;
    protected PetriDao<T, V> dao;

    private void generateSchema() throws SQLException, ClassNotFoundException {
        dbDriver = DBDriver.dbDriver(JDBC_H2_IN_MEM_CONNECTION_STRING);
        dbDriver.createSchema();

    }

    @Before
    public void setup() throws SQLException, ClassNotFoundException {
        generateSchema();
        objectMapper = ObjectMapperFactory.makeObjectMapper();
        mappingErrorHandler = context.mock(MappingErrorHandler.class);
    }

    @Test
    public void whenIllegalStringDeserializedReturnsNullAndReports() {
        context.checking(new Expectations() {{
            oneOf(mappingErrorHandler).handleError(with(containsString("illegal")), with(containsString("experiment")), with(any(IOException.class)));
        }});

        insertIllegalJson();

        List<T> list = dao.fetch();
        assertThat(list.size(), is(1));
        assertThat(list.get(0), is(nullValue()));
    }

    protected abstract void insertIllegalJson();

    @Test(expected = FullPetriClient.PetriException.class)
    public void serializationFailureThrowsException() throws JsonProcessingException {
        final PetriMapper mockMapper = context.mock(PetriMapper.class);
        final Object objToAdd = objectToAdd();
        context.checking((new Expectations() {{
            allowing(mockMapper).serialize(objToAdd);
            will(throwException(new Throwable("whatever")));
        }}));
        PetriDao dao = new JdbcSpecsDao(dbDriver.jdbcTemplate, mockMapper);
        dao.add(objToAdd);
    }

    protected abstract Object objectToAdd();
}
