package com.wixpress.petri.laboratory

import java.util.UUID

import com.natpryce.makeiteasy.MakeItEasy._
import com.wixpress.petri.amplitude.AmplitudeAdapter
import com.wixpress.petri.experiments.domain.{Assignment, TestGroup}
import com.wixpress.petri.laboratory.dsl.UserInfoMakers._
import com.wixpress.petri.laboratory.dsl.{ExperimentMakers, UserInfoMakers}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.{BeforeAfterAll, Scope}

// the stub for the amplitude wiremock is file-based - it's located in test/resources/*mapping.json
class AmplitudeTestGroupAssignmentTrackerIT extends SpecificationWithJUnit with BeforeAfterAll {
  val port = 11981
  val amplitudeDriver = new AmplitudeDriver(port)

  override def beforeAll(): Unit = amplitudeDriver.start()

  override def afterAll(): Unit = amplitudeDriver.stop()

  class Context extends Scope {
    val amplitudeAdapter = new AmplitudeAdapter(
      amplitudeUrl = s"http://localhost:$port/httpapi",
      apiKey = "198e3469868de498f5d67581d6de4518")

    val amplitudeTestGroupAssignmentTracker = new AmplitudeTestGroupAssignmentTracker(amplitudeAdapter)

    val userInfo = a(UserInfoMakers.UserInfo,
      `with`(userId, UUID.fromString("ba8f170a-fe2e-4453-92bf-e7a6aa6a1443")),
      `with`(ip, "THE_IP"),
      `with`(country, "THE_COUNTRY"),
      `with`(url, "THE_URL"),
      `with`(userAgent, "THE_UA"),
      `with`(language, "THE_LANGUAGE"),
      `with`(globalSessionId, "THE_GSI_NUMBER")).make

    val testGroup = new TestGroup(4, 0, "")
    val experimentId: java.lang.Integer = 123
    val experiment = a(ExperimentMakers.Experiment).but(
      `with`(ExperimentMakers.id, experimentId),
      `with`(ExperimentMakers.scope, "PRODUCT_NAME")
    ).make

    val assignment = new Assignment(userInfo, null, null, testGroup, experiment, 0)
  }

  "AmplitudeTestGroupAssignmentTracker" should {
    "upon newAssignment, post an http request to amplitude fake server" in new Context {
      amplitudeTestGroupAssignmentTracker.newAssignment(assignment)
      amplitudeDriver.assertThatAmplitudeWasCalled()
    }
  }
}

