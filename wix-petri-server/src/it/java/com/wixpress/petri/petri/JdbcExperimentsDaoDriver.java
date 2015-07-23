package com.wixpress.petri.petri;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSnapshot;
import org.joda.time.DateTime;
import util.DBDriver;

import static java.util.Arrays.asList;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class JdbcExperimentsDaoDriver {

    private JdbcExperimentsDao dao;
    private DBDriver dbDriver;
    private ObjectMapper objectMapper;

    public JdbcExperimentsDaoDriver(JdbcExperimentsDao dao, DBDriver dbDriver, ObjectMapper objectMapper) {
        this.dao = dao;
        this.dbDriver = dbDriver;
        this.objectMapper = objectMapper;
    }

    public Experiment addExperimentWithMatchingSpec(ExperimentSnapshot snapshot) throws JsonProcessingException {
        writeSpecToDB(snapshot);
        return dao.add(snapshot);
    }

    private void writeSpecToDB(ExperimentSnapshot snapshot) throws JsonProcessingException {
        dbDriver.insertSpec(objectMapper.writeValueAsString(new SpecDefinition.ExperimentSpecBuilder(snapshot.key(), new DateTime()).
                withTestGroups(asList("1", "2")).build()), snapshot.key());
    }
}
