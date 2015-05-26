package com.wixpress.petri.petri

import java.net.InetAddress
import java.util.concurrent.ScheduledExecutorService

import org.jmock.lib.concurrent.{DeterministicScheduler, Synchroniser}
import org.jmock.{Expectations, Mockery}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

import scala.collection.JavaConversions._
import scala.concurrent._
import scala.concurrent.duration._

/**
 * User: Dalias
 * Date: 12/30/14
 * Time: 11:32 AM
 */
class ServerMetricsReporterTest extends SpecificationWithJUnit  {

  trait Context extends Scope {

    val context = new Mockery()
    context.setThreadingPolicy(new Synchroniser())

    val petriClient = context.mock(classOf[PetriClient])
    var scheduler: ScheduledExecutorService = new DeterministicScheduler()

    val reporter = new ServerMetricsReporter(petriClient, scheduler , 1 )
  }

  "ServerMetricsReporter" should {

    "should aggregate multiple experiment counts " in new Context {
      reporter.reportConductExperiment(1, "true")
      reporter.reportConductExperiment(1, "true")
      reporter.reportConductExperiment(1, "true")

      context.checking(
        new Expectations() {
          oneOf (petriClient).reportConductExperiment(Seq(new ConductExperimentReport(InetAddress.getLocalHost.getHostName , 1, "true", 3l)))
        }
      )
      reporter.reportToServer()

      context.assertIsSatisfied()
    }

    "should aggregate different experiment values to different counts " in new Context {
      reporter.reportConductExperiment(1, "true")
      reporter.reportConductExperiment(1, "false")
      reporter.reportConductExperiment(1, "true")

      context.checking(
        new Expectations() {
          oneOf (petriClient).reportConductExperiment(Seq(
            new ConductExperimentReport(InetAddress.getLocalHost.getHostName , 1, "true", 2l),
          new ConductExperimentReport(InetAddress.getLocalHost.getHostName , 1, "false", 1l)))
        }
      )
      reporter.reportToServer()

      context.assertIsSatisfied()
    }

    "should not report to server when there are no counts " in new Context {
      reporter.reportToServer()

      context.assertIsSatisfied()
    }

    "should clear map after sending report   " in new Context {

      context.checking(
        new Expectations() {
          exactly(2).of (petriClient).reportConductExperiment(Seq(
            new ConductExperimentReport(InetAddress.getLocalHost.getHostName , 1, "true", 1l)))
        }
      )
      reporter.reportConductExperiment(1, "true")
      reporter.reportToServer()
      reporter.reportConductExperiment(1, "true")

      reporter.reportToServer()

      context.assertIsSatisfied()
    }

    "should run 100 concurrent writes properly " in new Context {
      import scala.concurrent.ExecutionContext.Implicits.global
      val futures = (1 to 100).map( x =>
        Future( reporter.reportConductExperiment(1, (x % 2 == 0).toString))
      )

      Await.result(Future.sequence(futures), 20.0.milliseconds)

      context.checking(
        new Expectations() {
          exactly(1).of (petriClient).reportConductExperiment(Seq(
            new ConductExperimentReport(InetAddress.getLocalHost.getHostName , 1, "true", 50l),
            new ConductExperimentReport(InetAddress.getLocalHost.getHostName , 1, "false", 50l)
          ))
        }
      )

      reporter.reportToServer()

      context.assertIsSatisfied()
    }
  }
}
