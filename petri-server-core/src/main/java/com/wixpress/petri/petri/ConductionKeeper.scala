package com.wixpress.petri.petri

import java.util.concurrent.{ScheduledExecutorService, TimeUnit}
import javax.mail.internet.InternetAddress
import com.wixpress.petri.experiments.domain.{Experiment, Trigger}
import scala.collection.JavaConversions._

class ConductionKeeper(clock: Clock, metricsReportsDao: MetricsReportsDao,
                       experimentsDao: ExperimentsDao,
                       scheduler: ScheduledExecutorService,
                       scheduledInterval: Long,
                       notifier: PetriNotifier){

  val triggerMessage = "Experiment paused due to conduction limit reach"
  val triggerOwner = "Conduction Keeper"
  val mailFromField = new InternetAddress("petri@wix.com")

  scheduler.scheduleAtFixedRate(runPauseExperimentIfConductionLimitReached(), scheduledInterval, scheduledInterval, TimeUnit.MILLISECONDS)

  val pauseTrigger:Trigger = new Trigger(triggerMessage, triggerOwner)


  private def runPauseExperimentIfConductionLimitReached(): Runnable = new Runnable {
    override def run(): Unit = pauseExperimentIfConductionLimitReached()
  }

  def pauseExperimentIfConductionLimitReached(): Unit = {
    val lastReportedExperimentConduction = metricsReportsDao.getReportedExperimentsSince(scheduledInterval)
    val allExperiments = experimentsDao.fetch()

    val experimentsToPause = getPauseCandidates(lastReportedExperimentConduction, allExperiments)

    experimentsToPause.foreach{ pauseCandidate =>
      val notifyMessage = createNotifyMessageAndTitle(pauseCandidate)
      val experiment = pauseCandidate.experiment
      val pausedExperiment = experiment.pause(pauseTrigger)
      updateExperimentInRepo(pausedExperiment)
      notifier.notify(notifyMessage.title, notifyMessage.message, mailFromField, true, Seq(notifyMessage.updaterEmail))
    }

  }

  private def updateExperimentInRepo(experiment: Experiment) = {
    experimentsDao.update(experiment, clock.getCurrentDateTime)
  }

  private def getPauseCandidates(lastReportedExperimentsConduction: List[TotalExperimentConduction],
                                                    allExperiments: Seq[Experiment]) :List[PauseCandidate] =  {

    lastReportedExperimentsConduction
      .map(x=> PauseCandidate(x, allExperiments.find(exp => exp.getId == x.experimentId).get))
      .filterNot(candidate => candidate.experiment.getExperimentSnapshot.conductLimit == 0)
      .filterNot(candidate => candidate.experiment.isPaused)
      .filter(candidate => candidate.conductionTotal.totalConduction >= candidate.experiment.getExperimentSnapshot.conductLimit)
  }

  private def createNotifyMessageAndTitle(pausedCandidate: PauseCandidate) = {
    val name = pausedCandidate.experiment.getName
    val id = pausedCandidate.experiment.getId
    val total = pausedCandidate.conductionTotal.totalConduction
    val limit = pausedCandidate.experiment.getExperimentSnapshot.conductLimit
    val message = s"Experiment:$name id:$id conduction:$total limit:$limit" //TODO move outside
    val title = s"Experiment $name id:$id paused due to conduction limit reach"

    NotifyMessage(message, title, pausedCandidate.experiment.getUpdater)
  }


  case class NotifyMessage(message: String, title: String, updaterEmail: String)
}
