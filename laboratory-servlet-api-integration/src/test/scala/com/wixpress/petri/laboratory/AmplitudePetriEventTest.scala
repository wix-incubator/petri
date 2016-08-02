package com.wixpress.petri.laboratory

import java.util.UUID

import com.natpryce.makeiteasy.MakeItEasy._
import com.wixpress.petri.experiments.domain.{Assignment, TestGroup}
import com.wixpress.petri.laboratory.dsl.{ExperimentMakers, UserInfoMakers}
import com.wixpress.petri.laboratory.dsl.UserInfoMakers._
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

class AmplitudePetriEventTest extends SpecificationWithJUnit {

  class Context extends Scope {
    val userUuid = UUID.randomUUID()

    val userInfo = a(UserInfoMakers.UserInfo,
      `with`(userId, userUuid),
      `with`(country, "THE_COUNTRY"),
      `with`(ip, "THE_IP"),
      `with`(url, "THE_URL"),
      `with`(userAgent, "THE_UA"),
      `with`(language, "THE_LANGUAGE")).make

    val testGroup = new TestGroup(4, 0, "")
    val experimentId: java.lang.Integer = 123
    val experiment = a(ExperimentMakers.Experiment).but(
      `with`(ExperimentMakers.id, experimentId),
      `with`(ExperimentMakers.scope, "PRODUCT_NAME")
    ).make

    val assignment = new Assignment(userInfo, null, null, testGroup, experiment, 0)
  }

  "AmplitudeEvent" should {
    "be created from an assignment" in new Context {
      AmplitudePetriEvent.fromAssignment(assignment) must be_===(AmplitudePetriEvent(
        eventType = AmplitudePetriEvent.petriBiEventType,
        language = "THE_LANGUAGE",
        country = "THE_COUNTRY",
        ip = "THE_IP",
        userId = userUuid.toString,
        eventProperties = AmplitudePetriEventProperties(
          experimentId = 123,
          productName = "PRODUCT_NAME",
          url = "THE_URL",
          userAgent = "THE_UA",
          testGroup = 4
        )
      ))
    }
  }
}