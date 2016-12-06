package com.wixpress.petri.petri

import java.sql._

import com.wixpress.petri.experiments.domain.{Experiment, ExperimentSnapshot}
import com.wixpress.petri.petri.FullPetriClient.{CreateFailed, CreateFailedData}
import com.wixpress.petri.petri.JdbcExperimentsDao._
import org.joda.time.DateTime
import org.springframework.jdbc.core._
import org.springframework.jdbc.support.GeneratedKeyHolder

import scala.collection.JavaConversions.asScalaBuffer
import scala.util.control.NonFatal

/**
 * @author dmitryk
 * @since 17-Sep-2015
 */
class JdbcExperimentsDao(jdbcTemplateRW: JdbcTemplate, jdbcTemplateRO: JdbcTemplate, mapper: PetriMapper[Experiment]) extends ExperimentsDao {

  def this(jdbcTemplate: JdbcTemplate, mapper: PetriMapper[Experiment]) { // for OSS compatibility
    this(jdbcTemplate, jdbcTemplate, mapper)
  }

  override def add(spec: ExperimentSnapshot): Experiment = {
    val serialized = serializeWrapException(spec)

    val keyHolder = new GeneratedKeyHolder
    jdbcTemplateRW.update(new InsertStatement(serialized, spec), keyHolder)

    if (keyHolder.getKey == null) {
      throw new CreateFailed(new CreateFailedData(spec.getClass.getSimpleName, spec.key))
    }

    val id = keyHolder.getKey.intValue()

    // set orig_id field as id unless already set
    jdbcTemplateRW.update("UPDATE experiments SET orig_id = ? WHERE id = ? AND orig_id = 0", Int.box(id), Int.box(id))

    jdbcTemplateRW.queryForObject(SELECT_SQL, mapper, Int.box(id))
  }

  override def update(experiment: Experiment, currentDateTime: DateTime): Unit = {
    val serializedExperiment = serializeWrapException(experiment.getExperimentSnapshot)
    val rowsAffected = jdbcTemplateRW.update(new UpdateStatement(experiment, serializedExperiment, currentDateTime))
    if (rowsAffected != 1) {
      throw new FullPetriClient.UpdateFailed(experiment)
    }
  }

  override def fetch(): Seq[Experiment] = {
    jdbcTemplateRO.query(FETCH_SQL, rsExtractor)
  }

  override def fetchByLastUpdate(from: DateTime, to: DateTime): Seq[Experiment] = {
    jdbcTemplateRO.query(FETCH_SQL_BY_LAST_UPDATE, rsExtractor, Long.box(getTimestamp(from)), Long.box(getTimestamp(to)))
  }

  override def fetchBetweenStartEndDates(now: DateTime): Seq[Experiment] = {
    jdbcTemplateRO.query(FETCH_SQL_FOR_BETWEEN_START_END, rsExtractor, Long.box(getTimestamp(now)))
  }

  override def getHistoryById(id: Int): Seq[Experiment] = {
    val originalId = jdbcTemplateRO.query(SELECT_SQL, rsExtractor, Int.box(id)).head.getOriginalId
    jdbcTemplateRO.query(HISTORY_SQL, rsExtractor, Int.box(originalId))
  }

  override def fetchAllExperimentsGroupedByOriginalId: Seq[Experiment] = {
    jdbcTemplateRO.query(FETCH_SQL_GROUPED_BY_ORIGINAL_ID, rsExtractor)
  }

  override def fetchExperimentById(experimentId: Int): Option[Experiment] = {
    jdbcTemplateRO.query(SELECT_SQL, rsExtractor, Int.box(experimentId)).headOption
  }

  override def fetchEndingBetween(from: DateTime, to: DateTime): Seq[Experiment] = {
    jdbcTemplateRO.query(FETCH_SQL_FOR_ENDING_BETWEEN, rsExtractor, Long.box(getTimestamp(from.minusSeconds(2))), Long.box(getTimestamp(to)))
  }

  override def migrateStartEndDates(): Int = {
    val experiments = jdbcTemplateRW
      .query("SELECT id, last_update_date, experiment FROM experiments WHERE start_date = 0", rsExtractor)
      .filter(_ != null)

    for (exp <- experiments) {
      val count = jdbcTemplateRW.update(
        "UPDATE experiments SET start_date = ?, end_date = ? WHERE id = ? AND last_update_date = ?",
        Long.box(getTimestamp(exp.getStartDate)), Long.box(getTimestamp(exp.getEndDate)),
        Int.box(exp.getId), Long.box(getTimestamp(exp.getLastUpdated)))
      require(count == 1, s"Updated rows for ${exp.getId}: $count")
    }

    experiments.size
  }

  private def rsExtractor = new RowMapperResultSetExtractor(mapper)

  private def serializeWrapException(obj: ExperimentSnapshot): String = try {
    mapper.serialize(obj)
  } catch {
    case NonFatal(e) => throw new FullPetriClient.PetriException(e)
  }
}

private object JdbcExperimentsDao {

  val SELECT_SQL = "SELECT id, last_update_date, experiment FROM experiments WHERE id = ? ORDER BY last_update_date DESC LIMIT 1"

  private val FETCH_SQL_FORMAT = "" +
    "SELECT experiments.id, experiments.last_update_date, experiments.experiment " +
    "FROM experiments " +
    "JOIN (SELECT id, MAX(last_update_date) AS ts FROM experiments %s GROUP BY id) maxt " +
    "ON (experiments.id = maxt.id AND experiments.last_update_date = maxt.ts)"

  val FETCH_SQL = String.format(FETCH_SQL_FORMAT, "")

  val FETCH_SQL_GROUPED_BY_ORIGINAL_ID = " " +
    "SELECT recents.id, recents.last_update_date, recents.experiment " +
    "FROM (" +
    "  SELECT experiments.id, experiments.last_update_date, experiments.experiment FROM experiments " +
    "  JOIN " +
    "  (SELECT id, MAX(last_update_date) AS ts FROM experiments GROUP BY id) maxt " +
    "  ON (experiments.id = maxt.id AND experiments.last_update_date = maxt.ts)) recents " +
    "JOIN (SELECT MAX(id) AS id FROM experiments GROUP BY orig_id) highest_orig " +
    "ON recents.id = highest_orig.id"

  val HISTORY_SQL = "SELECT id, last_update_date, experiment FROM experiments WHERE orig_id = ? ORDER BY last_update_date DESC"

  val FETCH_SQL_BY_LAST_UPDATE = String.format(FETCH_SQL_FORMAT, "WHERE last_update_date > ? AND last_update_date < ?")

  // end_date - 1 is to not include end_date (the same as Experiment#isActiveAt)
  val FETCH_SQL_FOR_BETWEEN_START_END = "" +
    "SELECT experiments.id, experiments.last_update_date, experiments.experiment " +
    "FROM experiments " +
    "JOIN (SELECT id, MAX(last_update_date) AS ts FROM experiments GROUP BY id) maxt " +
    "ON (experiments.id = maxt.id AND experiments.last_update_date = maxt.ts AND (? BETWEEN experiments.start_date AND experiments.end_date - 1))"

  //TODO - extract all the common group by id!!
  val FETCH_SQL_FOR_ENDING_BETWEEN = FETCH_SQL + " AND experiments.end_date BETWEEN ? AND ?"

  def getTimestamp(v: DateTime) = v.getMillis

  private def preparedStatementWithArgs(ps: PreparedStatement, args: List[Any]): PreparedStatement = {
    for (index <- 1 to args.size) {
      args(index - 1) match {
        case v: Int => ps.setInt(index, v)
        case v: DateTime => ps.setLong(index, getTimestamp(v))
        case v: String => ps.setString(index, v)
        case t => throw new IllegalStateException(s"Value of unsupported type ${t.getClass}: $t")
      }
    }
    ps
  }


  class InsertStatement(snapshotString: String, snapshot: ExperimentSnapshot) extends PreparedStatementCreator {
    @throws(classOf[SQLException])
    def createPreparedStatement(connection: Connection): PreparedStatement = {
      val specCond = if (snapshot.isFromSpec) Some(snapshot.key) else None

      val insertSql = "" +
        "INSERT INTO experiments (orig_id, last_update_date, experiment, start_date, end_date) " +
        "SELECT ?, ?, ?, ?, ?" +
        specCond.fold("")(_ => " FROM specs WHERE fqn = (?)")

      val values = List(snapshot.originalId, snapshot.creationDate, snapshotString, snapshot.startDate, snapshot.endDate)
      preparedStatementWithArgs(connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS), values ++ specCond)
    }
  }

  class UpdateStatement(experiment: Experiment, serializedExperiment: String, currentDateTime: DateTime) extends PreparedStatementCreator {
    @throws(classOf[SQLException])
    def createPreparedStatement(connection: Connection): PreparedStatement = {
      val specCond = if (experiment.isFromSpec) Some(experiment.getKey) else None

      val updateSql = "" +
        "INSERT INTO experiments (id, orig_id, last_update_date, experiment, start_date, end_date) " +
        "SELECT ?, ?, ?, ?, ?, ? " +
        "FROM experiments a " +
        "WHERE a.last_update_date = ?" +
        "  AND NOT EXISTS (SELECT 1 FROM experiments b WHERE b.id = a.id AND b.last_update_date > a.last_update_date)" +
        "  AND a.id = ?" +
        specCond.fold("")(_ => " AND EXISTS (SELECT 1 FROM specs WHERE fqn = ?)")

      val valuesForInsert = List(experiment.getId, experiment.getOriginalId, currentDateTime, serializedExperiment,
        experiment.getStartDate, experiment.getEndDate)
      val valuesForVersionCondition = List(experiment.getLastUpdated, experiment.getId)

      preparedStatementWithArgs(
        connection.prepareStatement(updateSql),
        valuesForInsert ++ valuesForVersionCondition ++ specCond
      )
    }
  }
}
