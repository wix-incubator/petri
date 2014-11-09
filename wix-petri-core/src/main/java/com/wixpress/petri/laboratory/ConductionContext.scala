package com.wixpress.petri.laboratory

import com.wixpress.petri.experiments.domain.AdditionalEligibilityCriteria


/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 3/31/14
 */


trait ConductionContext extends BIAdditions {

  def testGroupDrawer(fallback: TestGroupDrawer): TestGroupDrawer

  def biAdditions(): BIAdditions

  def additionalEligibilityCriteria(): AdditionalEligibilityCriteria
}

object ConductionContextBuilder {

  def newInstance = ConductionContextBuilder()

}

case class ConductionContextBuilder(customDrawer: Option[TestGroupDrawer] = None, biAdditions: BIAdditions = BIAdditions.Empty,
                                    additionalEligibilityCriteria: AdditionalEligibilityCriteria = new AdditionalEligibilityCriteria()) extends ConductionContext {
  def withTestGroupDrawer(testGroupDrawer: TestGroupDrawer): ConductionContextBuilder = copy(customDrawer = Option(testGroupDrawer))

  def withBIAdditions(biAdditions: BIAdditions): ConductionContextBuilder = copy(biAdditions = Option(biAdditions) getOrElse this.biAdditions)

  def withCriterionOverride[T](criterion: EligibilityCriterion[T]): ConductionContextBuilder = copy(additionalEligibilityCriteria = additionalEligibilityCriteria.withCriterion(criterion))

  def testGroupDrawer(fallback: TestGroupDrawer): TestGroupDrawer = customDrawer getOrElse fallback

  def contributeToBi(contributor: BIContributor): Unit = {
    biAdditions.contributeToBi(contributor)
  }
}

