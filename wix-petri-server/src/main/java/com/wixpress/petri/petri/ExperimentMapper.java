package com.wixpress.petri.petri;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentBuilder;
import com.wixpress.petri.experiments.domain.ExperimentSnapshot;
import org.joda.time.DateTime;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class ExperimentMapper extends JsonPetriMapper<Experiment> {

    public ExperimentMapper(ObjectMapper objectMapper, MappingErrorHandler mappingErrorHandler) {
        super(objectMapper, mappingErrorHandler);
    }

    @Override
    public Experiment mapRow(ResultSet rs, int rowNum) throws SQLException {
        ExperimentSnapshot snapshot = deserialize(json(rs), new TypeReference<ExperimentSnapshot>() {
        }, "experiment");
        return (snapshot == null) ? null : ExperimentBuilder.anExperiment().
                withId(id(rs)).
                withLastUpdated(lastUpdated(rs)).
                withExperimentSnapshot(snapshot).build();
    }

    private int id(ResultSet rs) throws SQLException {
        return rs.getInt(1);
    }

    private DateTime lastUpdated(ResultSet rs) throws SQLException {
        return new DateTime(rs.getLong(2));
    }

    private String json(ResultSet rs) throws SQLException {
        return rs.getString(3);
    }

}
