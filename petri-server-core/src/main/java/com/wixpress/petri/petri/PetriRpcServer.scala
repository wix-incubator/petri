package com.wixpress.petri.petri

import java.util
import java.util.UUID

import com.wixpress.petri.experiments.domain.ExperimentBuilder.aCopyOf
import com.wixpress.petri.experiments.domain.{Experiment, ExperimentSnapshot, ExperimentSpec}
import com.wixpress.petri.petri.Exceptions.ExperimentNotFoundException
import com.wixpress.petri.petri.PetriRpcServer._
import org.joda.time.DateTime

import scala.collection.JavaConversions.seqAsJavaList
import scala.collection.JavaConverters._
import scala.util.control.NonFatal

/**
 * @author talyag
 * @since 9/9/13
 */
class PetriRpcServer(experimentsDao: ExperimentsDao,
                     clock: Clock,
                     specsDao: SpecsDao,
                     petriNotifier: PetriNotifier,
                     metricsReportsDao: MetricsReportsDao,
                     userStateDao: UserStateDao)
  extends FullPetriClient
  with PetriClient
  with UserRequestPetriClient
  with PetriDeveloperApi {

  override def fetchActiveExperiments(): util.List[Experiment] = {
    // check isActiveAt remains to not break not migrated stagings
    val now = clock.getCurrentDateTime
    experimentsDao.fetchBetweenStartEndDates(now).filter(exp => exp != null && exp.isActiveAt(now))
  }

  override def fetchAllExperiments(): util.List[Experiment] = {
    fetchNotNullExperiments()
  }

  override def fetchAllExperimentsGroupedByOriginalId(): util.List[Experiment] = {
    experimentsDao.fetchAllExperimentsGroupedByOriginalId
  }

  override def fetchRecentlyEndedExperimentsDueToEndDate(minutesEnded: Int): util.List[Experiment] = {
    val now = new DateTime
    val intervalStart = now.minusMinutes(minutesEnded)
    experimentsDao.fetchEndingBetween(intervalStart, now).filter(isTerminatedDueToEndDateReached)
  }

  override def fetchExperimentsGoingToEndDueToDate(minutesEnded: Int): util.List[Experiment] = {
    val now = new DateTime
    val intervalEnd = now.plusMinutes(minutesEnded)
    experimentsDao.fetchEndingBetween(now, intervalEnd)
  }

  private def fetchNotNullExperiments(): Seq[Experiment] = {
    experimentsDao.fetch().filter(exp => exp != null)
  }

  override def insertExperiment(snapshot: ExperimentSnapshot): Experiment = {
    experimentsDao.add(snapshot)
  }

  override def updateExperiment(experiment: Experiment): Experiment = {
    val now = clock.getCurrentDateTime
    experimentsDao.update(experiment, now)
    aCopyOf(experiment).withLastUpdated(now).build
  }

  override def fetchSpecs(): util.List[ExperimentSpec] = {
    specsDao.fetch().filter(_ != null)
  }

  override def addSpecs(specs: util.List[ExperimentSpec]) {
    addSpecs(specs, validate = true)
  }

  override def addSpecNoValidation(spec: ExperimentSpec) = {
    addSpecs(Seq(spec), validate = false)
  }

  override def getHistoryById(id: Int): util.List[Experiment] = {
    experimentsDao.getHistoryById(id)
  }

  override def deleteSpec(key: String) {
    if (!hasUnterminatedExperiments(key)) {
      specsDao.delete(key)
    }
  }

  override def reportConductExperiment(conductExperimentReports: util.List[ConductExperimentReport]) {
    metricsReportsDao.addReports(conductExperimentReports)
  }

  override def saveUserState(userId: UUID, userState: String) {
    userStateDao.saveUserState(userId, userState, clock.getCurrentDateTime)
  }

  override def getUserState(userId: UUID): String = {
    userStateDao.getUserState(userId)
  }

  override def getExperimentReport(experimentId: Int): util.List[ConductExperimentSummary] = {
    metricsReportsDao.getReport(experimentId)
  }

  override def getLatestExperimentReportsAfterUpdateDate(lastConductedDate: DateTime): util.List[java.lang.Integer] = {
    metricsReportsDao.getExperimentIdsLastConductedAfterGivenDate(lastConductedDate: DateTime)
  }

  private def addSpecs(specs: util.List[ExperimentSpec], validate: Boolean) {
    val existingSpecs = specsDao.fetch()
    for (spec <- specs.asScala) {
      try {
        addSpec(spec, existingSpecs, validate)
      } catch {
        case e: Exception if NonFatal(e) =>
          notifyOfFailure(spec, e)
          e.printStackTrace()
      }
    }
  }

  private def addSpec(experimentSpec: ExperimentSpec, existingSpecs: Seq[ExperimentSpec], validate: Boolean) {
    if (specSnapshotExists(experimentSpec, existingSpecs)) {
      return
    }

    val originalSpecOpt = existingSpecs.find(exp => exp.hasSameKey(experimentSpec.getKey))

    if (validate) {
      if (hasUnterminatedExperiments(experimentSpec.getKey)) {
        notifyOfUpdateFailure(experimentSpec, originalSpecOpt)
        return
      }
    }

    originalSpecOpt.fold(addNew(experimentSpec))(originalSpec => updateExisting(experimentSpec, originalSpec))
  }

  private def notifyOfFailure(experimentSpec: ExperimentSpec, e: Exception) {
    petriNotifier.notify(printSpecUpdateFailedMsg(experimentSpec.getKey), e.toString, experimentSpec.getOwner)
  }

  private def notifyOfUpdateFailure(experimentSpec: ExperimentSpec, originalSpec: Option[ExperimentSpec]) {
    petriNotifier.notify(
      printSpecUpdateFailedMsg(experimentSpec.getKey) + " - " + printNonTerminatedExperimentsMsg,
      printOriginalAndNewSpecs(experimentSpec, originalSpec),
      experimentSpec.getOwner
    )
  }

  private def specSnapshotExists(experimentSpec: ExperimentSpec, existingSpecs: Seq[ExperimentSpec]): Boolean = {
    existingSpecs.exists(es => es.getExperimentSpecSnapshot == experimentSpec.getExperimentSpecSnapshot)
  }

  private def hasUnterminatedExperiments(specKey: String): Boolean = {
    fetchNotNullExperiments().exists(exp => exp.hasSameKey(specKey) && !exp.isTerminated)
  }

  private def addNew(experimentSpec: ExperimentSpec) {
    specsDao.add(experimentSpec)
  }

  private def updateExisting(experimentSpec: ExperimentSpec, originalSpec: ExperimentSpec) {
    val now = clock.getCurrentDateTime
    specsDao.update(experimentSpec.setCreationDate(originalSpec.getCreationDate), now)
    notifyOwnerIfNeeded(experimentSpec, originalSpec)
  }

  private def notifyOwnerIfNeeded(experimentSpec: ExperimentSpec, originalSpec: ExperimentSpec) {
    if (originalSpec.getOwner != experimentSpec.getOwner) {
      petriNotifier.notify(
        printSpecOwnerChangedMsg(experimentSpec.getKey, experimentSpec.getOwner),
        printOriginalAndNewSpecs(experimentSpec, Some(originalSpec)),
        originalSpec.getOwner
      )
    }
  }

  private def isTerminatedDueToEndDateReached(experiment: Experiment): Boolean = {
    val delta = 60*1000
    Math.abs(experiment.getEndDate.getMillis - experiment.getLastUpdated.getMillis) > delta
  }

  override def getFullUserState(userGuid: UUID): UserState = {
    userStateDao.getFullUserState(userGuid)
  }

  override def migrateStartEndDates(): Unit = {
    experimentsDao.migrateStartEndDates()
  }

  @throws(classOf[ExperimentNotFoundException])
  override def fetchExperimentById(experimentId: Int): Experiment = experimentsDao.fetchExperimentById(experimentId).
    getOrElse(throw new ExperimentNotFoundException("Cannot find experiment with id = " + experimentId + "."))

}

private[petri] object PetriRpcServer {
  def printSpecOwnerChangedMsg(key: String, owner: String) = s"Pay attention - Owner of $key has been changed to $owner"

  def printSpecUpdateFailedMsg(key: String) = s"Failed to update spec [$key]"

  def printNonTerminatedExperimentsMsg = "Cannot update spec when non-terminated experiments exist on it." +
    " " +
    "If you actually want the new version you need to: terminate the existing experiment/s on this spec and then rescan your specs (via GA or manual POST request)"

  def printOriginalAndNewSpecs(experimentSpec: ExperimentSpec, originalSpec: Option[ExperimentSpec]) =
    s"Previous spec - [${originalSpec.orNull}]"+
    "  " +
    s"New spec      - [$experimentSpec]"
}
