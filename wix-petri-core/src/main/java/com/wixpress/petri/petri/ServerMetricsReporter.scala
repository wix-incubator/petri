package com.wixpress.petri.petri

import java.net.InetAddress
import java.util.concurrent.{TimeUnit, ScheduledExecutorService}
import java.util.concurrent.atomic.AtomicLong

import scala.collection.JavaConversions._
import scala.collection.concurrent.TrieMap

/**
 * User: Dalias
 * Date: 12/7/14
 * Time: 3:32 PM
 */

trait MetricsReporter {
  def reportConductExperiment(experimentId: Int, experimentValue: String)
  def reportToServer()
}

class ServerMetricsReporter(petriClient: PetriClient, scheduler: ScheduledExecutorService, scheduledInterval: Long)  extends MetricsReporter{

  val reportsMap = TrieMap[ReportKey, AtomicLong]()

  override def reportConductExperiment(experimentId: Int, experimentValue: String) {
    val reportKey = new ReportKey(experimentId, experimentValue)
    //This needs to be synchoronized because getOrElseUpdate isn't atomic. We should remove this after the bug is fixed :
    // https://issues.scala-lang.org/browse/SI-7943   (should be fixed in 2.11.6)
    var counter  =  this.synchronized {
       reportsMap.getOrElseUpdate(reportKey, new AtomicLong())
    }
    counter.addAndGet(1)

  }


  override def reportToServer() {
    val hostName: String = InetAddress.getLocalHost.getHostName
    val conductExperimentReports: Seq[ConductExperimentReport] = reportsMap.map { case (key, counter) =>
      new ConductExperimentReport(hostName, key.experimentId, key.experimentValue, counter.get())
    }.toSeq
    if (conductExperimentReports.nonEmpty)
      petriClient.reportConductExperiment(conductExperimentReports)
    reportsMap.clear()

  }

  def startScheduler() {
    scheduler.scheduleAtFixedRate(runReport(), scheduledInterval, scheduledInterval, TimeUnit.MILLISECONDS)
  }

  def stopScheduler() {
    reportToServer()
    scheduler.shutdown()
  }

  private def runReport(): Runnable = new Runnable {
    override def run(): Unit = reportToServer()
  }


}

case class ReportKey(experimentId: Int, experimentValue: String)


