package com.wixpress.petri.laboratory

import com.wixpress.petri.experiments.domain.EligibilityFields


/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 3/31/14
 */


trait ConductContext extends BIAdditions {
  def testGroupDrawer(fallback: TestGroupDrawer): TestGroupDrawer

  def biAdditions(): BIAdditions

  def eligibilityFields(): EligibilityFields
}

case class ConductContextBuilder(customDrawer: Option[TestGroupDrawer] = None, biAdditions: BIAdditions = BIAdditions.Empty,
                                 eligibilityFields: EligibilityFields = new EligibilityFields()) extends ConductContext {
  def withTestGroupDrawer(testGroupDrawer: TestGroupDrawer): ConductContextBuilder = copy(customDrawer = Option(testGroupDrawer))

  def withBIAdditions(biAdditions: BIAdditions): ConductContextBuilder = copy(biAdditions = Option(biAdditions) getOrElse this.biAdditions)

  def withField(field: EligibilityField): ConductContextBuilder = copy(eligibilityFields = eligibilityFields.withField(field))

  def testGroupDrawer(fallback: TestGroupDrawer): TestGroupDrawer = customDrawer getOrElse fallback

  def contributeToBi(contributor: BIContributor): Unit = {
    biAdditions.contributeToBi(contributor)
  }
}

object ConductContextBuilder {

  def newInstance = ConductContextBuilder()

}
