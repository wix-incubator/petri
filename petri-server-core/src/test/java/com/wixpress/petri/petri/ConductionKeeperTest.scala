package com.wixpress.petri.petri

import java.util.concurrent.TimeUnit
import javax.mail.internet.InternetAddress

import com.natpryce.makeiteasy.MakeItEasy
import com.natpryce.makeiteasy.MakeItEasy._
import com.wixpress.petri.experiments.domain.ExperimentBuilder._
import com.wixpress.petri.experiments.domain.{Experiment, ExperimentSnapshot}
import com.wixpress.petri.laboratory.dsl.ExperimentMakers
import com.wixpress.petri.laboratory.dsl.ExperimentMakers._
import org.jmock.lib.concurrent.DeterministicScheduler
import org.joda.time.DateTime
import org.specs2.matcher.ThrownExpectations
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
import com.wixpress.common.specs2.JMock
import scala.collection.JavaConversions._


class ConductionKeeperTest extends SpecificationWithJUnit with JMock {

  "conduction keeper" should {

    "pause experiment after it reached / exceeds its conduction limit and notify relevant users" in new Context {

      val conductionLimitForExperiment = totalExperimentConductionForExperiment(experimentWithConductionLimit, limit + 1)
      val conductionLimitForAnotherExperiment = totalExperimentConductionForExperiment(anotherExperimentWithConductionLimit, limit + 1)

      allowingClockInteraction(2)

      assumingMetricsRepoContains(List(conductionLimitForExperiment, conductionLimitForAnotherExperiment))
      assumingExperimentRepoContains(List(experimentNoConductionLimit, experimentWithConductionLimit, anotherExperimentWithConductionLimit))

      conductionKeeperUpdatesRepoWith(paused(experimentWithConductionLimit), someTimeAfterExperimentsWereCreated)
      conductionKeeperUpdatesRepoWith(paused(anotherExperimentWithConductionLimit), someTimeAfterExperimentsWereCreated)

      petriNotifiesAboutPausedExperiment(experimentWithConductionLimit, conductionLimitForExperiment)
      petriNotifiesAboutPausedExperiment(anotherExperimentWithConductionLimit, conductionLimitForAnotherExperiment)

      timeGoesBy(forwardMillis = schedulerInterval)
    }

    "not pause an experiment with no limit" in new Context {
      assumingMetricsRepoContains(List(totalExperimentConductionForExperiment(experimentNoConductionLimit, limit)))
      assumingExperimentRepoContains(List(experimentNoConductionLimit))

      timeGoesBy(forwardMillis = schedulerInterval)
    }

    "not pause a paused experiment" in new Context {
      assumingMetricsRepoContains(List(
        totalExperimentConductionForExperiment(pausedExperimentWithNoConductionLimit, limit + 1),
        totalExperimentConductionForExperiment(pausedExperimentWithConductionLimit, limit - 1)
      ))

      assumingExperimentRepoContains(List(pausedExperimentWithConductionLimit, pausedExperimentWithNoConductionLimit))

      timeGoesBy(forwardMillis = schedulerInterval)
    }

    "not pause experiment that didnt reach conduction limit" in new Context {
      assumingMetricsRepoContains(List(
        totalExperimentConductionForExperiment(experimentWithConductionLimit, limit - 1)))

      assumingExperimentRepoContains(List(experimentWithConductionLimit))

      timeGoesBy(forwardMillis = schedulerInterval)
    }
  }


  abstract class Context extends Scope with ThrownExpectations {

    def timeGoesBy(forwardMillis: Long) {
      scheduler.tick(forwardMillis, TimeUnit.MILLISECONDS)
    }

    def conductionKeeperUpdatesRepoWith(experiment: Experiment, time: DateTime) = {
      checking {
        oneOf(experimentsDao).update(experiment, time)
      }
    }

    def petriNotifiesAboutPausedExperiment(experiment: Experiment, totalExperimentConduction: TotalExperimentConduction) = {
      val name = experiment.getName
      val total = totalExperimentConduction.totalConduction
      val id = experiment.getId
      val limit = experiment.getExperimentSnapshot.conductLimit
      val message = s"Experiment:$name id:$id conduction:$total limit:$limit"
      val title = s"Experiment $name id:$id paused due to conduction limit reach"
      checking {
        oneOf(notifier).notify(title, message, new InternetAddress("petri@wix.com"),true, Seq(experiment.getUpdater) )
      }
    }

    def assumingExperimentRepoContains(returnList: Seq[Experiment]) = {
      checking {
        oneOf(experimentsDao).fetch() willReturn returnList
      }
    }

    def allowingClockInteraction(numOfInteractions: Int) = {
      checking {
        exactly(numOfInteractions).of(clock).getCurrentDateTime willReturn someTimeAfterExperimentsWereCreated
      }
    }

    def assumingMetricsRepoContains(returnList: List[TotalExperimentConduction]) = {
      checking {
        oneOf(metricsReportsDao).getReportedExperimentsSince(schedulerInterval) willReturn returnList
      }
    }

    def buildRecipients(user: String) = {
      new MailRecipients(recipients.to + new InternetAddress(user), recipients.cc)
    }

    val someTimeAfterExperimentsWereCreated = new DateTime

    val schedulerInterval = 1l
    val limit: java.lang.Integer = 2


    val metricsReportsDao = mock[MetricsReportsDao]
    val clock: Clock = mock[Clock]
    val notifier: PetriNotifier = mock[PetriNotifier]
    val recipients: MailRecipients = new MailRecipients(Set(new InternetAddress("r1@wix.com")), Set(new InternetAddress("r2@wix.com")))
    val experimentsDao = mock[ExperimentsDao]
    var scheduler: DeterministicScheduler = new DeterministicScheduler()
    val conductionKeeper = new ConductionKeeper(clock, metricsReportsDao, experimentsDao, scheduler, schedulerInterval, notifier)

    val someCreator = "some1@wix.com"
    val someUpdater = "some2@wix.com"


    val experimentWithConductionLimit: Experiment = an(ExperimentMakers.Experiment, MakeItEasy.`with`(id, makeId(1)), MakeItEasy.`with`(conductionLimit, limit), MakeItEasy.`with`(updater, someUpdater), MakeItEasy.`with`(creator, someCreator)).make
    val anotherExperimentWithConductionLimit: Experiment = an(ExperimentMakers.Experiment, MakeItEasy.`with`(id, makeId(2)), MakeItEasy.`with`(conductionLimit, limit), MakeItEasy.`with`(updater, someUpdater), MakeItEasy.`with`(creator, someCreator)).make
    val experimentNoConductionLimit: Experiment = an(ExperimentMakers.Experiment, MakeItEasy.`with`(id, makeId(3))).make
    val pausedExperimentWithConductionLimit: Experiment = paused(an(ExperimentMakers.Experiment, MakeItEasy.`with`(id, makeId(4))).make)
    val pausedExperimentWithNoConductionLimit: Experiment = paused(an(ExperimentMakers.Experiment, MakeItEasy.`with`(id, makeId(5))).make)

    def paused(experiment: Experiment) = {
      import conductionKeeper._
      aCopyOf(experiment).withExperimentSnapshot(
        experiment.getExperimentSnapshot.copy(paused = true, comment = triggerMessage, updater = triggerOwner))
        .build()
    }

    def totalExperimentConductionForExperiment(experiment: Experiment, total: Int) = TotalExperimentConduction(experiment.getId, total)

    def makeId(id: Int): java.lang.Integer = id.asInstanceOf[java.lang.Integer]
  }

}
