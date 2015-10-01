package com.wixpress.petri.petri

import org.joda.time.DateTime
import org.specs2.matcher.{Matcher, Scope}
import org.specs2.mutable.SpecificationWithJUnit

import scala.collection.JavaConversions._

/**
 * User: Dalias
 * Date: 12/10/14
 * Time: 4:45 PM
 */
class JdbcMetricsReportsDaoIT extends SpecificationWithJUnit  {
  sequential

  val JDBC_H2_IN_MEM_CONNECTION_STRING: String = "jdbc:h2:mem:test"

  trait Context extends Scope{
    val dbDriver = DBDriver.dbDriver(JDBC_H2_IN_MEM_CONNECTION_STRING)
    val metricsReportDao = new JdbcMetricsReportsDao (dbDriver.jdbcTemplate, 0l)
    dbDriver.createSchema()
    val simpleExperimentReport: ConductExperimentReport = new ConductExperimentReport("myServer", 11, "the value", 3l)
    val anotherSimpleExperimentReport: ConductExperimentReport = new ConductExperimentReport("myServer", 12, "the value", 5l)
  }

  "Metrics Reports Dao " should {

    "get single experiment conduction when only 1 experiment reported" in new Context {
      metricsReportDao.addReports(List(simpleExperimentReport))
      val experimentConduction = metricsReportDao.getReportedExperimentsSince(scheduledInterval = 500000l).last
      experimentConduction.experimentId must beEqualTo(simpleExperimentReport.experimentId)
      experimentConduction.totalConduction must beEqualTo(simpleExperimentReport.count)
    }

    "get multiple experiment conduction when some experiments reported" in new Context {
      metricsReportDao.addReports(List(simpleExperimentReport, anotherSimpleExperimentReport))
      metricsReportDao.addReports(List(anotherSimpleExperimentReport))
      val experimentConductionList = metricsReportDao.getReportedExperimentsSince(scheduledInterval = 500000l)
      val simpleExperimentConductTotal = experimentConductionList.filter(exp => exp.experimentId == simpleExperimentReport.experimentId)
      val anotherSimpleExperimentTotal = experimentConductionList.filter(exp => exp.experimentId == anotherSimpleExperimentReport.experimentId)

      simpleExperimentConductTotal.size must beEqualTo(1)
      anotherSimpleExperimentTotal.size must beEqualTo(1)

      simpleExperimentConductTotal.last.totalConduction must beEqualTo(simpleExperimentReport.count)
      anotherSimpleExperimentTotal.last.totalConduction must beEqualTo(anotherSimpleExperimentReport.count * 2)
    }

    "get empty list when no experiments were reported" in new Context {
      metricsReportDao.addReports(List(simpleExperimentReport))
      metricsReportDao.getReportedExperimentsSince(scheduledInterval = 0) must beEmpty
    }

     "Create a single report record successfully" in new Context  {
       
       metricsReportDao.addReports(List(simpleExperimentReport))

       metricsReportDao.getReport(simpleExperimentReport.experimentId).toList must contain(aConductExperimentSummary(simpleExperimentReport))
     }

    "Update a single report record successfully" in new Context  {


       metricsReportDao.addReports(List(simpleExperimentReport))
       metricsReportDao.addReports(List(simpleExperimentReport.copy(count = 5l)))

       metricsReportDao.getReport(simpleExperimentReport.experimentId).toList must contain(aConductExperimentSummary(simpleExperimentReport, 8l , 5l))
     }

    "Create a couple of report records successfully" in new Context  {

      private val reportWithBValue: ConductExperimentReport = simpleExperimentReport.copy(experimentValue = "b value", count = 2l)
      metricsReportDao.addReports(List(simpleExperimentReport, reportWithBValue))

      metricsReportDao.getReport(simpleExperimentReport.experimentId).toList must contain(aConductExperimentSummary(simpleExperimentReport), aConductExperimentSummary(reportWithBValue))
    }

    "Update a couple of report records successfully" in new Context  {

      private val reportWithBValue: ConductExperimentReport = simpleExperimentReport.copy(experimentValue = "b value", count = 2l)
      metricsReportDao.addReports(List(simpleExperimentReport, reportWithBValue))

      //update both reports
      metricsReportDao.addReports(List(simpleExperimentReport.copy(count = 7l), reportWithBValue.copy(count = 9l)))

      metricsReportDao.getReport(simpleExperimentReport.experimentId).toList must contain(aConductExperimentSummary(simpleExperimentReport, 10l, 7l), aConductExperimentSummary(reportWithBValue, 11l, 9l))
    }

  }


  private def aConductExperimentSummary(report : ConductExperimentReport) : Matcher[ConductExperimentSummary] =
    aConductExperimentSummary(report, report.count, report.count)

  private def  aConductExperimentSummary(report : ConductExperimentReport, totalCount: Long, fiveMinuteCount : Long) : Matcher[ConductExperimentSummary] =
  {(summary : ConductExperimentSummary) => summary.experimentId }  ^^ beEqualTo(report.experimentId)   and
  {(summary : ConductExperimentSummary) => summary.experimentValue }  ^^ beEqualTo(report.experimentValue)   and
  {(summary : ConductExperimentSummary) => summary.totalCount }  ^^ beEqualTo(totalCount)   and
  {(summary : ConductExperimentSummary) => summary.fiveMinuteCount }  ^^ beEqualTo(fiveMinuteCount)  and
  {(summary : ConductExperimentSummary) => summary.lastUpdated.getMillis }  ^^ beBetween(new DateTime().minusMinutes(1).getMillis, new DateTime().plusMinutes(1).getMillis)


}
