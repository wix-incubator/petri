package com.wixpress.guineapig.entities.ui

import com.wixpress.petri.petri.ConductExperimentSummary
import org.joda.time.DateTime
import org.specs2.matcher.Matcher
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

import scala.collection.JavaConversions._

class ExperimentReportBuilderTest extends SpecificationWithJUnit {

  trait Context extends Scope {
    val now: DateTime = DateTime.now
  }

  "ExperimentReportBuilder " should {
    "Build a report from one summary successfully" in new Context {
      val summary = ConductExperimentSummary("host", 1, "testGroupValue", 1, 1, now)
      val report = ExperimentReportBuilder.buildReport(1, List(summary))

      report must beAnExperimentReport(1, 1, 1, now)

      report.reportsPerValue must exactly(
        beAConductionValueReport("testGroupValue", 1, 1, now) and haveAServerReport("host", 1, 1, now)
      )
    }

    "Build a report from two summaries on same value" in new Context {
      val earlierReportDate = now.minusMinutes(1)
      val earlierSummary = ConductExperimentSummary("host", 1, "testGroupValue", 1, 1, earlierReportDate)
      val summary = ConductExperimentSummary("host", 1, "testGroupValue", 2, 6, now)

      val report = ExperimentReportBuilder.buildReport(1, List(earlierSummary, summary))

      report must beAnExperimentReport(1, 3, 7, now)
      report.reportsPerValue must exactly(beAConductionValueReport("testGroupValue", 3, 7, now))
    }

    "Use latest update date for per value report" in new Context {
      val earlierReportDate = now.minusMinutes(1)
      val earlierSummary = ConductExperimentSummary("host", 1, "testGroupValue", 1, 1, earlierReportDate)
      val summary = ConductExperimentSummary("host", 1, "testGroupValue", 2, 6, now)

      val report = ExperimentReportBuilder.buildReport(1, List(summary, earlierSummary))

      report must beAnExperimentReport(1, 3, 7, now)
      report.reportsPerValue must exactly(beAConductionValueReport("testGroupValue", 3, 7, now))
    }

    "Count only recent reports when summing five minutes per value" in new Context {
      val tooEarlyReportDate = now.minusMinutes(6)
      val tooEarlySummary = ConductExperimentSummary("host", 1, "testGroupValue", 1, 1, now)
      val summary = ConductExperimentSummary("host", 1, "testGroupValue", 2, 6, tooEarlyReportDate)

      val report = ExperimentReportBuilder.buildReport(1, List(tooEarlySummary, summary))

      report must beAnExperimentReport(1, 1, 7, now)
      report.reportsPerValue must exactly(beAConductionValueReport("testGroupValue", 1, 7, now))
    }

    "Build a report from two summaries on different values" in new Context {

      val earlierReportDate: DateTime = now.minusMinutes(1)
      val earlierSummary = ConductExperimentSummary("host", 1, "testGroupValue1", 1, 1, earlierReportDate)
      val summary = ConductExperimentSummary("host", 1, "testGroupValue2", 2, 6, now)

      val report = ExperimentReportBuilder.buildReport(1, List(earlierSummary, summary))

      report must beAnExperimentReport(1, 3, 7, now)
      report.reportsPerValue must exactly(
        beAConductionValueReport("testGroupValue1", 1, 1, earlierReportDate),
        beAConductionValueReport("testGroupValue2", 2, 6, now)
      )
    }

    "Count only recent reports when summing five minutes per server" in new Context {

      val earlierReportDate: DateTime = now.minusMinutes(1)
      val earlierSummary = ConductExperimentSummary("host1", 1, "testGroupValue1", 1, 1, earlierReportDate)
      val tooEarlyReportDate: DateTime = now.minusMinutes(6)
      val tooEarlySummary = ConductExperimentSummary("host1", 1, "testGroupValue1", 8, 8, tooEarlyReportDate)
      val summary = ConductExperimentSummary("host2", 1, "testGroupValue2", 2, 6, now)

      val report = ExperimentReportBuilder.buildReport(1, List(earlierSummary, summary, tooEarlySummary))

      report must beAnExperimentReport(1, 3, 15, now)
      report.reportsPerValue must exactly(
        beAConductionValueReport("testGroupValue1", 1, 9, earlierReportDate) and haveAServerReport("host1", 1, 9, earlierReportDate),
        beAConductionValueReport("testGroupValue2", 2, 6, now) and haveAServerReport("host2", 2, 6, now)
      )
    }

    "Build an empty report successfully" in new Context {
      val report = ExperimentReportBuilder.buildReport(1, List())

      report must beAnExperimentReport(1, 0, 0, new DateTime(0))
      report.reportsPerValue must beEmpty
    }


  }

  def beAnExperimentReport(experimentId: Int, fiveMinuteCount: Long, totalCount: Long, lastUpdated: DateTime): Matcher[ExperimentReport] = {
    { (report: ExperimentReport) => report.experimentId } ^^ beEqualTo(experimentId) and { (report: ExperimentReport) => report.fiveMinuteCount } ^^ beEqualTo(fiveMinuteCount) and { (report: ExperimentReport) => report.totalCount } ^^ beEqualTo(totalCount) and { (report: ExperimentReport) => report.lastUpdated } ^^ beEqualTo(lastUpdated)
  }

  def beAConductionValueReport(value: String, fiveMinuteCount: Long, totalCount: Long, lastUpdated: DateTime): Matcher[PerValueReport] = {
    { (report: PerValueReport) => report.experimentValue } ^^ beEqualTo(value) and { (report: PerValueReport) => report.fiveMinuteCount } ^^ beEqualTo(fiveMinuteCount) and { (report: PerValueReport) => report.totalCount } ^^ beEqualTo(totalCount) and { (report: PerValueReport) => report.lastUpdated } ^^ beEqualTo(lastUpdated)
  }

  def haveAServerReport(host: String, fiveMinuteCount: Long, totalCount: Long, lastUpdated: DateTime): Matcher[PerValueReport] = {
    { (report: PerValueReport) => report.reportsPerServer } ^^ contain(PerServerReport(host, fiveMinuteCount, totalCount, lastUpdated))
  }

}
