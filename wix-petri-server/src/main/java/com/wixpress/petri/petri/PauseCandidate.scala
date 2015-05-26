package com.wixpress.petri.petri

import com.wixpress.petri.experiments.domain.Experiment

case class PauseCandidate(conductionTotal: TotalExperimentConduction, experiment: Experiment)
