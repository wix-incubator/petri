package com.wixpress.petri

import com.wixpress.petri.experiments.domain.{Experiment, ExperimentSnapshot}

/**
 * Created by Nimrod_Lahav on 4/20/15.
 */
package object petri {
  type ExperimentsDao = OriginalIDAwarePetriDao[Experiment, ExperimentSnapshot]
}
