package com.wixpress.guineapig.services

import java.{util => ju}

import com.wixpress.guineapig.entities.ui.UiSpec
import com.wixpress.petri.experiments.domain.{Experiment, ExperimentSpec}
import com.wixpress.petri.petri.FullPetriClient
import org.springframework.beans.factory.annotation.Autowired

import scala.collection.JavaConversions._

class PetriSpecService extends SpecService {

  @Autowired
  var fullPetriClient: FullPetriClient = _

  def this(petriClient: FullPetriClient) {
    this
    fullPetriClient = petriClient
  }

  override def addSpecs(experimentSpec: ju.List[ExperimentSpec]) {
    fullPetriClient.addSpecs(experimentSpec)
  }

  override def deleteSpec(specKey: String) {
    fullPetriClient.deleteSpec(specKey)
  }

  override def getAllSpecs: ju.List[UiSpec] = {
    val allExperiments = fullPetriClient.fetchAllExperiments()

    val specs: ju.List[ExperimentSpec] = fullPetriClient.fetchSpecs
    specs.map(spec => new UiSpec(spec.getKey, spec.getTestGroups, spec.getOwner, spec.getCreationDate.getMillis, spec.getUpdateDate.getMillis, spec.getScopes.map(_.getName), !isSpecActive(spec.getKey, allExperiments), spec.isPersistent)
    )
  }

  override def isSpecActive(specKey: String, allExperiments: ju.List[Experiment]) = {
    val activeSpecs: Set[String] = allExperiments.filter(!_.isTerminated).filter(_.isFromSpec).map(_.getKey).toSet
    activeSpecs.contains(specKey)
  }
}