package com.wixpress.petri.petri

import java.net.InetAddress
import java.util.concurrent.{TimeUnit, ScheduledExecutorService}

import org.jmock.{Expectations, Mockery}
import org.jmock.lib.concurrent.DeterministicScheduler
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
      val futures = (1 to 100).map( range => {
        val experimentValue = range match {
          case n : Int if n % 2 == 0 => "true"
          case   _ => "false"
        }
        future { reporter.reportConductExperiment(1, experimentValue) }

      })

      futures.foreach(Await.result(_, Duration(2, TimeUnit.MILLISECONDS)))

      context.checking(
        new Expectations() {
          exactly(2).of (petriClient).reportConductExperiment(Seq(
            new ConductExperimentReport(InetAddress.getLocalHost.getHostName , 1, "true", 50l),
            new ConductExperimentReport(InetAddress.getLocalHost.getHostName , 1, "false", 50l)
          ))
        }
      )

      reporter.reportToServer()

    }
  }
}
