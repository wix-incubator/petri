package com.wixpress.petri.laboratory

import java.util.UUID

import com.natpryce.makeiteasy.MakeItEasy._
import com.wixpress.petri.experiments.domain.{Assignment, TestGroup}
import com.wixpress.petri.google_analytics.GoogleAnalyticsAdapterBuilder
import com.wixpress.petri.laboratory.dsl.UserInfoMakers._
import com.wixpress.petri.laboratory.dsl.{ExperimentMakers, UserInfoMakers}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.{BeforeAfterAll, Scope}

// the stub for the google analytics wiremock is file-based - it's located in test/resources/*mapping.json
class GoogleAnalyticsTestGroupAssignmentTrackerIT extends SpecificationWithJUnit with BeforeAfterAll {
  val port = 11981
  val googleAnalyticsDriver = new BiServerDriver(port)

  override def beforeAll(): Unit = googleAnalyticsDriver.start()

  override def afterAll(): Unit = googleAnalyticsDriver.stop()

  class Context extends Scope {
    val googleAnalyticsAdapter = GoogleAnalyticsAdapterBuilder.create(
      s"http://localhost:$port/collect", "UA-89204848-1", null)

    val googleAnalyticsTestGroupAssignmentTracker = new BiTestGroupAssignmentTracker(googleAnalyticsAdapter)

    val userInfo = a(UserInfoMakers.UserInfo,
      `with`(userId, UUID.fromString("ba8f170a-fe2e-4453-92bf-e7a6aa6a1443")),
      `with`(ip, "THE_IP"),
      `with`(country, "THE_COUNTRY"),
      `with`(url, "THE_URL"),
      `with`(userAgent, "THE_UA"),
      `with`(language, "THE_LANGUAGE"),
      `with`(globalSessionId, "THE_GSI_NUMBER")).make

    val testGroup = new TestGroup(1, 0, "red")
    val experimentId: java.lang.Integer = 123
    val experiment = a(ExperimentMakers.Experiment).but(
      `with`(ExperimentMakers.id, experimentId),
      `with`(ExperimentMakers.scope, "PRODUCT_NAME")
    ).make

    val assignment = new Assignment(userInfo, null, null, testGroup, experiment, 0)
  }

  "GoogleAnalyticsTestGroupAssignmentTracker" should {
    "upon newAssignment, post an http request to google analytics fake server" in new Context {
      googleAnalyticsTestGroupAssignmentTracker.newAssignment(assignment)
      googleAnalyticsDriver.assertThatBiServerWasCalled("collect")
    }
  }
}
