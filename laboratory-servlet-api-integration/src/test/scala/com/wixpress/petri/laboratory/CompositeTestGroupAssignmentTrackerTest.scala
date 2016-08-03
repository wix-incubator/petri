package com.wixpress.petri.laboratory

import com.natpryce.makeiteasy.MakeItEasy
import com.natpryce.makeiteasy.MakeItEasy._
import com.wixpress.common.specs2.JMock
import com.wixpress.petri.experiments.domain._
import com.wixpress.petri.laboratory.dsl.ExperimentMakers
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

class CompositeTestGroupAssignmentTrackerTest extends SpecificationWithJUnit with JMock {

  class Context extends Scope {

    val tracker1 = mock[TestGroupAssignmentTracker]("t1")
    val tracker2 = mock[TestGroupAssignmentTracker]("t2")

    val compositeTracker = CompositeTestGroupAssignmentTracker.create(tracker1).add(tracker2)

    val experimentId: java.lang.Integer = 123

    val experiment = a(ExperimentMakers.Experiment).but(
      MakeItEasy.`with`(ExperimentMakers.id, experimentId),
      MakeItEasy.`with`(ExperimentMakers.scope, "PRODUCT_NAME")
    ).make

    def createSomeAssignment() = new Assignment(null, null, null, null, experiment, 0)
  }

  "CompositeTestGroupAssignmentTracker" should {
    "delegate new assignment to child trackers" in new Context {
      val assignment = createSomeAssignment()

      checking {
        oneOf(tracker1).newAssignment(assignment)
        oneOf(tracker2).newAssignment(assignment)
      }

      compositeTracker.newAssignment(assignment)
    }
  }


}
