package com.wixpress.petri.petri

import com.wixpress.petri.experiments.domain.ScopeDefinition.aScopeDefinitionForAllUserTypes
import com.wixpress.petri.petri.SpecDefinition.ExperimentSpecBuilder
import org.joda.time.DateTime
import org.specs2.mutable.SpecWithJUnit

import scala.collection.JavaConverters._

class SpecDefinitionTest extends SpecWithJUnit {

  "SpecDefinition" should {

    "support custom key via same package subclassing" >> {
      new CustomKeySpecDefinition().create(new DateTime).getKey must be_===("custom-key")
    }
  }
}

class CustomKeySpecDefinition extends SpecDefinition {

  private[petri] override def generateSpecKey(): String = "custom-key"

  override protected def customize(builder: ExperimentSpecBuilder): ExperimentSpecBuilder = {
    builder
      .withScopes(aScopeDefinitionForAllUserTypes("some-scope"))
      .withOwner("some@owner.com")
      .withTestGroups(List("g1", "g2").asJava)
  }
}
