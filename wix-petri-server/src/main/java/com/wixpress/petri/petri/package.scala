package com.wixpress.petri

import com.wixpress.petri.experiments.domain.{Experiment, ExperimentSnapshot}

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
package object petri {
  type ExperimentsDao = OriginalIDAwarePetriDao[Experiment, ExperimentSnapshot]
}
