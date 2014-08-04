package com.wixpress.petri.laboratory


/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 3/31/14
 */
trait ConductContext extends BIAdditions {
  def testGroupDrawer(fallback: TestGroupDrawer): TestGroupDrawer
  def biAdditions(): BIAdditions
}

trait ConductContextBuilding extends ConductContext {
  def withTestGroupDrawer(testGroupDrawer: TestGroupDrawer): ConductContextBuilding
  def withBIAdditions(biAdditions: BIAdditions): ConductContextBuilding
}

object ConductContextBuilder {

  def newInstance = ABuilder()

  case class ABuilder(customDrawer: Option[TestGroupDrawer] = None, biAdditions: BIAdditions = BIAdditions.Empty) extends ConductContextBuilding {
    def withTestGroupDrawer(testGroupDrawer: TestGroupDrawer): ABuilder = copy(customDrawer = Option(testGroupDrawer))
    def withBIAdditions(biAdditions: BIAdditions): ABuilder = copy(biAdditions = Option(biAdditions) getOrElse this.biAdditions)
    def testGroupDrawer(fallback: TestGroupDrawer): TestGroupDrawer = customDrawer getOrElse fallback
    def contributeToBi(contributor: BIContributor): Unit = {
      biAdditions.contributeToBi(contributor)
    }
  }
}
