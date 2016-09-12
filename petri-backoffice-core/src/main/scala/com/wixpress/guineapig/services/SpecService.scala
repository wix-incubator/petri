package com.wixpress.guineapig.services

import java.{util => ju}

import com.wixpress.guineapig.entities.ui.UiSpec
import com.wixpress.petri.experiments.domain.{Experiment, ExperimentSpec}

/**
 * User: Dalias
 * Date: 11/16/15
 * Time: 9:40 AM
 */
trait SpecService {

   def addSpecs(experimentSpec: ju.List[ExperimentSpec])
   def deleteSpec(specKey: String)
   def getAllSpecs: ju.List[UiSpec]
   def isSpecActive(specKey: String, allExperiments: ju.List[Experiment]): Boolean
}
