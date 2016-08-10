package com.wixpress.petri.laboratory

import com.wixpress.petri.experiments.domain.Assignment

class CompositeTestGroupAssignmentTracker private(trackers: Seq[TestGroupAssignmentTracker]) extends TestGroupAssignmentTracker {
  override def newAssignment(assignment: Assignment): Unit =
    trackers.foreach(_.newAssignment(assignment))

  def add(tracker:TestGroupAssignmentTracker) = new CompositeTestGroupAssignmentTracker(trackers :+ tracker)
}

object CompositeTestGroupAssignmentTracker {
  def create(tracker: TestGroupAssignmentTracker) = new CompositeTestGroupAssignmentTracker(Seq(tracker))
}
