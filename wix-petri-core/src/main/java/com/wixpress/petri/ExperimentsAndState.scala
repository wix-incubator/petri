package com.wixpress.petri

import com.wixpress.petri.experiments.domain.Experiment

case class ExperimentsAndState(experiments: java.util.List[Experiment], stale: Boolean)  {

  def staleOrEmpty: Boolean = stale || experiments.isEmpty
}