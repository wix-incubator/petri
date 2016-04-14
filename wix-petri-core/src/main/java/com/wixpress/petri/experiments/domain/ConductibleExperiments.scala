package com.wixpress.petri.experiments.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
case class ConductibleExperiments(experiments: java.util.List[Experiment])