package com.wixpress.petri.petri;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wixpress.petri.experiments.domain.Experiment;
import com.wixpress.petri.experiments.domain.ExperimentSnapshot;
import org.joda.time.DateTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.wixpress.petri.petri.FullPetriClient.PetriException;
import static com.wixpress.petri.petri.FullPetriClient.UpdateFailed;

/**
 * @author: talyag
 * @since: 9/15/13
 */
public class JdbcExperimentsDao extends JdbcPetriDao<Experiment, ExperimentSnapshot> implements OriginalIDAwarePetriDao<Experiment, ExperimentSnapshot> {

    public static final String FETCH_SQL = "select experiments.id, experiments.last_update_date, experiments.experiment " +
            " from experiments join " +
            " (select id ,max(last_update_date) as ts from experiments %s group by id) maxt" +
            " on (experiments.id = maxt.id and experiments.last_update_date = maxt.ts)";

    public static final String FETCH_SQL_GROUPED_BY_ORIGINAL_ID =
            " select recents.id, recents.last_update_date, recents.experiment " +
                    " from (" +
                    " select experiments.id, experiments.last_update_date, experiments.experiment from experiments join " +
                    " (select id ,max(last_update_date) as ts from experiments group by id) maxt" +
                    " on (experiments.id = maxt.id and experiments.last_update_date = maxt.ts)) recents " +
                    " join (select max(id) as id from experiments group by orig_id) highest_orig on" +
                    " (recents.id = highest_orig.id)";

    public static final String HISTORY_SQL =
            "select id, last_update_date, experiment from experiments where orig_id = %s order by last_update_date desc";

    private final String FECTH_SQL_FOR_INTERVAL = String.format(FETCH_SQL,
            "where last_update_date > %s and last_update_date < %s");

    public JdbcExperimentsDao(JdbcTemplate jdbcTemplate, PetriMapper mapper) {
        super(jdbcTemplate, mapper,
                String.format(FETCH_SQL, ""),
                "select id, last_update_date, experiment from experiments where id = %s order by last_update_date desc"
        );
    }

    @Override
    protected String identifierOf(ExperimentSnapshot obj) {
        return obj.key();
    }

    @Override
    protected InsertStatement createInsertStatement(String serializedObj, ExperimentSnapshot obj) {
        return new InsertStatement(serializedObj, obj);
    }

    @Override
    public List<Experiment> getHistoryById(int id) {
        int originalId = ((Experiment) jdbcTemplate.query(String.format(selectSql, id), mapper).get(0)).getOriginalId();
        return jdbcTemplate.query(String.format(HISTORY_SQL, originalId), mapper);
    }

    protected static class InsertStatement implements PreparedStatementCreator {
        private final String snapshotString;
        private final ExperimentSnapshot snapshot;

        public InsertStatement(String snapshotString, ExperimentSnapshot snapshot) {
            this.snapshotString = snapshotString;
            this.snapshot = snapshot;
        }

        public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
            String insertSql = "INSERT into experiments(orig_id, last_update_date, experiment) " +
                    "select ?,?,?";
            if (snapshot.isFromSpec()) {
                insertSql += " from specs WHERE fqn = (?)";
            }

            PreparedStatement ps = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, snapshot.originalId());
            ps.setLong(2, snapshot.creationDate().getMillis());
            ps.setString(3, snapshotString);

            if (snapshot.isFromSpec()) {
                ps.setString(4, snapshot.key());
            }
            return ps;
        }
    }

    protected void postUpdate(int id) {
        // set orig_id field as id unless already set
        jdbcTemplate.update("UPDATE experiments set orig_id =? where id=? and orig_id=0", id, id);
    }

    @Override
    public void update(final Experiment experiment, final DateTime currentDateTime) {
        final String serializedExperiment;
        try {
            serializedExperiment = mapper.serialize(experiment.getExperimentSnapshot());
        } catch (JsonProcessingException e) {
            throw new PetriException(e);
        }

        final int experimentId = experiment.getId();
        final long lastUpdated = getTimestamp(experiment.getLastUpdated());

        String updateSql = "INSERT into experiments(id, orig_id, last_update_date, experiment) " +
                "select ?,?,?,? from experiments a " +
                "where a.last_update_date = ? and not exists (select 1 from experiments b where b.id = a.id and b.last_update_date > a.last_update_date) and a.id=?";

        if (experiment.isFromSpec()) {
            updateSql += " and exists (select 1 from specs where fqn = ?)";
        }

        List<Object> insertValues = Arrays.<Object>asList(experimentId, experiment.getOriginalId(), getTimestamp(currentDateTime), serializedExperiment);
        List<Object> versionValidationValues = Arrays.<Object>asList(lastUpdated, experimentId);
        List<Object> specValidationValues = experiment.isFromSpec() ? Arrays.<Object>asList(experiment.getKey()) : Collections.emptyList();

        List<Object> updateSqlArgs = newArrayList(insertValues);
        updateSqlArgs.addAll(versionValidationValues);
        updateSqlArgs.addAll(specValidationValues);

        int rowsAffected = jdbcTemplate.update(updateSql, updateSqlArgs.toArray());

        if (rowsAffected != 1) {
            throw new UpdateFailed(experiment, experiment.getId());
        }
    }

    private long getTimestamp(DateTime currentDateTime) {
        return currentDateTime.getMillis();
    }

    //TODO - pull up to an interface
    public List<Experiment> fetchInInterval(DateTime from, DateTime to) {
        return jdbcTemplate.query(String.format(FECTH_SQL_FOR_INTERVAL, from.getMillis(), to.getMillis()), mapper);
    }

    @Override
    public List<Experiment> fetchAllExperimentsGroupedByOriginalId() {
        return jdbcTemplate.query(FETCH_SQL_GROUPED_BY_ORIGINAL_ID, mapper);
    }


}
