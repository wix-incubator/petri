package com.wixpress.petri.laboratory

import com.wixpress.petri.experiments.domain.AdditionalEligibilityCriteria


/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 3/31/14
 */


trait ConductionContext{

  def additionalEligibilityCriteria(): AdditionalEligibilityCriteria
  def biAdditions(): BIAdditions
  def customConductionStrategy() : Option[ConductionStrategy]

  def conductionStrategyOrFallback(fallback: ConductionStrategy): ConductionStrategy
}

object ConductionContext {

  def mergeSecondWins(first: ConductionContext, second: ConductionContext) = {
    ConductionContextBuilder(
      biAdditions = BIAdditions.merge(first.biAdditions, second.biAdditions),

      additionalEligibilityCriteria = AdditionalEligibilityCriteria.merge(first.additionalEligibilityCriteria, second.additionalEligibilityCriteria),

      customConductionStrategy = second.customConductionStrategy match {
        case Some(cs) => second.customConductionStrategy
        case None => first.customConductionStrategy
      }
    )
  }
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



