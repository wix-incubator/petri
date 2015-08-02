package com.wixpress.petri.petri;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wixpress.petri.experiments.domain.ExperimentSpec;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author: talyag
 * @since: 9/30/13
 */
public class SpecMapper extends JsonPetriMapper<ExperimentSpec> {

    public SpecMapper(ObjectMapper objectMapper, MappingErrorHandler mappingErrorHandler) {
        super(objectMapper, mappingErrorHandler);
    }

    @Override
    public ExperimentSpec mapRow(ResultSet rs, int rowNum) throws SQLException {
        return deserialize(json(rs), new TypeReference<ExperimentSpec>() {
        }, "experiment spec");
    }

    private String json(ResultSet rs) throws SQLException {
        return rs.getString(1);
    }
}
