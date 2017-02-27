package com.wixpress.guineapig.entities.ui

import java.io.IOException
import java.{util => ju}

import com.wixpress.guineapig.spi.FilterAdapterExtender
import com.wixpress.petri.experiments.domain.ExperimentSnapshotBuilder.anExperimentSnapshot
import com.wixpress.petri.experiments.domain._
import org.joda.time.DateTime

import scala.collection.JavaConverters._

object UiExperimentConverter {
  @throws(classOf[IOException])
  def toExperiment(experiment: UiExperiment, isNew: Boolean, spec: ExperimentSpec, hardCodedScopes: ju.List[ScopeDefinition], filterAdapterExtender: FilterAdapterExtender, userName: String): Experiment = {
    val experimentSnapshotBuilder: ExperimentSnapshotBuilder = getExperimentSnapshot(experiment, isNew, spec, hardCodedScopes.asScala.toList, filterAdapterExtender, userName)
    return ExperimentBuilder.anExperiment.withId(experiment.getId).withLastUpdated(new DateTime(experiment.getLastUpdated)).withExperimentSnapshot(experimentSnapshotBuilder.build).build
  }

  @throws(classOf[IOException])
  private def getExperimentSnapshot(experiment: UiExperiment, isNew: Boolean, spec: ExperimentSpec, hardCodedScopes: List[ScopeDefinition], filterAdapterExtender: FilterAdapterExtender, sessionUser: String): ExperimentSnapshotBuilder = {
    val filters = ExperimentFilterBuilder.extractFiltersFromUiExperiment(experiment).asScala ++ filterAdapterExtender.extractFiltersFromUiExperiment(experiment).asScala

    val testGroups = new ju.ArrayList[TestGroup]
    import scala.collection.JavaConversions._
    for (uiTestGroup <- experiment.getGroups) {
      testGroups.add(uiTestGroup.toTestGroup)
    }
    var experimentSnapshotBuilder: ExperimentSnapshotBuilder = anExperimentSnapshot.
      withCreator(experiment.getCreator).
      withName(experiment.getName).
      withKey(experiment.getKey).
      withFromSpec(experiment.isSpecKey).
      withOriginalId(experiment.getOriginalId).
      withLinkedId(experiment.getLinkId).
      withDescription(experiment.getDescription).
      withComment(experiment.getComment).
      withUpdater(sessionUser).
      withStartDate(new DateTime(experiment.getStartDate)).
      withEndDate(new DateTime(experiment.getEndDate)).
      withGroups(testGroups).
      withScopes(experiment.getScope.split(",").toList).
      withFilters(filters).
      withPaused(experiment.isPaused).
      withFeatureToggle(experiment.getType == ExperimentType.FEATURE_TOGGLE.getType).
      withConductLimit(experiment.getConductLimit)
    if (isNew) {
      experimentSnapshotBuilder = experimentSnapshotBuilder.
        withCreationDate(new DateTime).
        withOriginalId(Experiment.NO_ID).
        withCreator(sessionUser)
    }
    else {
      experimentSnapshotBuilder = experimentSnapshotBuilder.withCreationDate(new DateTime(experiment.getCreationDate))
    }
    experimentSnapshotBuilder = addNonUiFields(experiment.getScope, spec, hardCodedScopes, experimentSnapshotBuilder)
    experimentSnapshotBuilder
  }

  private def addNonUiFields(scope: String, spec: ExperimentSpec, hardCodedScopes: List[ScopeDefinition], experimentSnapshotBuilder: ExperimentSnapshotBuilder): ExperimentSnapshotBuilder = {
    if (spec != null) {
      experimentSnapshotBuilder.withPersistent(spec.isPersistent).withAllowedForBots(spec.isAllowedForBots)
    }
    setRegisteredUsersOnly(scope, spec, hardCodedScopes, experimentSnapshotBuilder)
  }

  private def setRegisteredUsersOnly(scope: String, spec: ExperimentSpec, hardCodedScopes: List[ScopeDefinition], experimentSnapshotBuilder: ExperimentSnapshotBuilder): ExperimentSnapshotBuilder = {
    val scopesFromSpec = if (spec != null) {
      spec.getScopes.asScala
    } else List()
    val possibleScopes = hardCodedScopes ++ scopesFromSpec
    val scopeDefiniton = possibleScopes.find(possibleScope => scope.split(",").contains(possibleScope.getName))

    scopeDefiniton.fold(
      throw new IllegalArgumentException(s"you cannot create an experiment without some scopeDefinition - how did you even get here?")
    ) (scopeDef => experimentSnapshotBuilder.withOnlyForLoggedInUsers(scopeDef.isOnlyForLoggedInUsers))
  }
}