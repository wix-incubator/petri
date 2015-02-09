package com.wixpress.petri.petri

import java.sql.ResultSet
import java.{util => ju}

import org.joda.time.DateTime
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.jdbc.core.{RowMapper, JdbcTemplate}
import scala.collection.JavaConversions._

/**
 * User: Dalias
 * Date: 12/10/14
 * Time: 4:30 PM
 */
trait MetricsReportsDao {
  def getReport(experimentId: Int): ju.List[ConductExperimentSummary]

  def addReports(conductExperimentReports: ju.List[ConductExperimentReport])

  val metricsReportsMapper = new MetricsReportsMapper
}

class JdbcMetricsReportsDao(jdbcTemplate: JdbcTemplate) extends MetricsReportsDao {

  private val addReportQuery = "INSERT INTO metricsReport (server_name, experiment_id, experiment_value, total_count, five_minutes_count, last_update_date)  VALUES (?,?,?,?,?,?) "
  private val updateReportQuery = "UPDATE metricsReport SET total_count = ? , five_minutes_count = ? , last_update_date = ? where server_name = ? AND experiment_id = ?  AND experiment_value = ?"
  private val getReportByExperimentID = "SELECT * FROM metricsReport WHERE experiment_id = ? "
  private val getReportByPrimaryKey = "SELECT * FROM metricsReport WHERE server_name = ? AND experiment_id = ?  AND experiment_value = ?"


  override def addReports(conductExperimentReports: ju.List[ConductExperimentReport]) {
    conductExperimentReports.foreach(report => {
      getUniqueReport(report) match {
        case None => jdbcTemplate.update(addReportQuery, report.serverName, asInt(report.experimentId) , report.experimentValue, report.count, report.count, asLong((new DateTime).getMillis))
        case Some(result) => jdbcTemplate.update(updateReportQuery, asLong(result.totalCount + report.count), report.count, asLong((new DateTime).getMillis), report.serverName, asInt(report.experimentId), report.experimentValue)
      }

    })
  }

  override def getReport(experimentId: Int): ju.List[ConductExperimentSummary] =
     jdbcTemplate.query(getReportByExperimentID, metricsReportsMapper, asInt(experimentId))


  private def getUniqueReport(conductExperimentReport: ConductExperimentReport): Option[ConductExperimentSummary] =
    try{
      Some(jdbcTemplate.queryForObject(getReportByPrimaryKey, metricsReportsMapper, conductExperimentReport.serverName, asInt(conductExperimentReport.experimentId), conductExperimentReport.experimentValue))
    } catch {
      case ex: IncorrectResultSizeDataAccessException => None
      case ex => throw ex
    }

  private def asLong(num : Long) : java.lang.Long = num
  private def asInt(num : Int) : java.lang.Integer = num
}

class MetricsReportsMapper extends RowMapper[ConductExperimentSummary] {
  override def mapRow(rs: ResultSet, rowNum: Int): ConductExperimentSummary = new ConductExperimentSummary(
    rs.getString("server_name"), rs.getInt("experiment_id"), rs.getString("experiment_value"), rs.getLong("five_minutes_count"), rs.getLong("total_count"), new DateTime(rs.getLong("last_update_date")))
}
