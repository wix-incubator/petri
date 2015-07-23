package com.wixpress.petri.laboratory

import com.wixpress.petri.experiments.domain.AdditionalEligibilityCriteria


/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */


trait ConductionContext{

  def conductionStrategyOrFallback(fallback: ConductionStrategy): ConductionStrategy
  def additionalEligibilityCriteria(): AdditionalEligibilityCriteria
  def biAdditions(): BIAdditions
}

object ConductionContextBuilder {

  def newInstance = ConductionContextBuilder()

}

case class ConductionContextBuilder(biAdditions: BIAdditions = BIAdditions.Empty,
                                    additionalEligibilityCriteria: AdditionalEligibilityCriteria = new AdditionalEligibilityCriteria(),
                                    customConductionStrategy: Option[ConductionStrategy] = None) extends ConductionContext {

  def withBIAdditions(biAdditions: BIAdditions): ConductionContextBuilder = copy(biAdditions = Option(biAdditions) getOrElse this.biAdditions)
  def withConductionStrategy(conductionStrategy : ConductionStrategy): ConductionContextBuilder = copy(customConductionStrategy = Option(conductionStrategy))
  def withCriterionOverride[T](criterion: EligibilityCriterion[T]): ConductionContextBuilder = copy(additionalEligibilityCriteria = additionalEligibilityCriteria.withCriterion(criterion))

  def conductionStrategyOrFallback(fallback: ConductionStrategy): ConductionStrategy = customConductionStrategy getOrElse fallback

}



