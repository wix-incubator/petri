package com.wixpress.petri.petri;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wixpress.petri.experiments.domain.ExperimentSpec;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
public class JdbcSpecsDao extends JdbcPetriDao<ExperimentSpec, ExperimentSpec> implements DeleteEnablingPetriDao<ExperimentSpec, ExperimentSpec> {

    public JdbcSpecsDao(JdbcTemplate jdbcTemplate, PetriMapper mapper) {
        super(jdbcTemplate, mapper,
                "select spec from specs",
                "select spec from specs where id = %s"
        );
    }

    protected String identifierOf(ExperimentSpec obj) {
        return obj.getKey();
    }

    @Override
    protected InsertStatement createInsertStatement(String serializedObj, ExperimentSpec obj) {
        return new InsertStatement(serializedObj, identifierOf(obj));
    }

    @Override
    public void update(ExperimentSpec spec, DateTime currentDateTime) {
        try {
            jdbcTemplate.update("update specs " +
                    "set spec = ?" +
                    "where fqn = ?", mapper.serialize(spec), spec.getKey());
        } catch (JsonProcessingException e) {
            e.printStackTrace();  // TODO - This should be rethrown as UpdateFailedExcpetion() and be ignored
        }
    }

    protected static class InsertStatement implements PreparedStatementCreator {
        private final String spec;
        private String key;

        public InsertStatement(String spec, String key) {
            this.spec = spec;
            this.key = key;
        }

        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
            PreparedStatement ps =
                    connection.prepareStatement("INSERT into specs(fqn,spec) values (?,?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, key);
            ps.setString(2, spec);
            return ps;
        }
    }

    @Override
    public void delete(String spec) {
        jdbcTemplate.update("delete from specs where fqn = ?", spec);
    }

}
