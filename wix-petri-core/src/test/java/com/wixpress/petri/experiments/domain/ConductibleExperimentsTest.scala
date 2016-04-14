package com.wixpress.petri.experiments.domain

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.collect.ImmutableList
import com.natpryce.makeiteasy.MakeItEasy._
import com.wixpress.petri.experiments.jackson.ObjectMapperFactory
import com.wixpress.petri.laboratory.dsl.ExperimentMakers
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

class ConductibleExperimentsTest extends SpecificationWithJUnit {

  trait Context extends Scope {
      val objectMapper = ObjectMapperFactory.makeObjectMapper()
      val conductibleExperiments = new ConductibleExperiments(ImmutableList.of(an(ExperimentMakers.Experiment).make()))
  }

  "ConductibleExperiments" should {
    "be serialized and deSerialized correctly" in new Context {
      val json = objectMapper.writeValueAsString(conductibleExperiments)
      val deSerialized = objectMapper.readValue(json, classOf[ConductibleExperiments])
      deSerialized ==== conductibleExperiments
    }

    "be forward compatible" in new Context {
      val json = objectMapper.writeValueAsString(conductibleExperiments)
      val jsonTree = objectMapper.readTree(json)

      jsonTree.asInstanceOf[ObjectNode].put("newFutureField", "someFutureValue")
      val deSerialized = objectMapper.treeToValue(jsonTree, classOf[ConductibleExperiments])

      deSerialized ==== conductibleExperiments
    }
  }
}
