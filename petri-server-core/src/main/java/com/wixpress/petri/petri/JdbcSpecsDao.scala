package com.wixpress.petri.petri

import java.sql.{Connection, PreparedStatement, SQLException, Statement}

import com.fasterxml.jackson.core.JsonProcessingException
import com.wixpress.petri.experiments.domain.ExperimentSpec
import com.wixpress.petri.petri.FullPetriClient.{CreateFailed, CreateFailedData}
import com.wixpress.petri.petri.JdbcSpecsDao._
import org.joda.time.DateTime
import org.springframework.jdbc.core.{JdbcTemplate, PreparedStatementCreator, RowMapperResultSetExtractor}
import org.springframework.jdbc.support.GeneratedKeyHolder

import scala.collection.JavaConversions.asScalaBuffer
import scala.util.control.NonFatal

/**
 * @author dmitryk
 * @since 21-Sep-2015
 */
class JdbcSpecsDao(jdbcTemplate: JdbcTemplate, mapper: PetriMapper[ExperimentSpec]) extends SpecsDao {

  override def add(spec: ExperimentSpec): ExperimentSpec = {
    val serialized = serializeWrapException(spec)

    val keyHolder = new GeneratedKeyHolder
    jdbcTemplate.update(new InsertStatement(spec.getKey, serialized), keyHolder)

    if (keyHolder.getKey == null) {
      throw new CreateFailed(new CreateFailedData(spec.getClass.getSimpleName, spec.getKey))
    }

    jdbcTemplate.queryForObject("SELECT spec FROM specs WHERE id = ?", mapper, keyHolder.getKey)
  }

  override def update(spec: ExperimentSpec, currentDateTime: DateTime): Unit = {
    try {
      jdbcTemplate.update("UPDATE specs SET spec = ? WHERE fqn = ?", mapper.serialize(spec), spec.getKey)
    } catch {
      case e: JsonProcessingException =>
        // TODO - This should be rethrown as UpdateFailedExcpetion() and be ignored
        e.printStackTrace()
    }
  }

  override def delete(key: String): Unit = {
    jdbcTemplate.update("DELETE FROM specs WHERE fqn = ?", key)
  }

  override def fetch(): Seq[ExperimentSpec] = {
    jdbcTemplate.query("SELECT spec FROM specs", new RowMapperResultSetExtractor(mapper))
  }

  private def serializeWrapException(obj: ExperimentSpec): String = try {
    mapper.serialize(obj)
  } catch {
    case NonFatal(e) => throw new FullPetriClient.PetriException(e)
  }

}

private object JdbcSpecsDao {

  class InsertStatement(key: String, spec: String) extends PreparedStatementCreator {
    @throws(classOf[SQLException])
    def createPreparedStatement(connection: Connection): PreparedStatement = {
      val ps = connection.prepareStatement("INSERT INTO specs (fqn, spec) values (?, ?)", Statement.RETURN_GENERATED_KEYS)
      ps.setString(1, key)
      ps.setString(2, spec)
      ps
    }
  }

}
