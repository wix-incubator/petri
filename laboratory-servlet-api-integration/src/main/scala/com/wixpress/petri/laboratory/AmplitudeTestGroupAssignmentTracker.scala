package com.wixpress.petri.laboratory

import java.net.URLEncoder
import java.util.UUID

import com.fasterxml.jackson.annotation.JsonProperty
import com.wixpress.petri.amplitude.{AmplitudeAdapter, BaseAmplitudeEvent}
import com.wixpress.petri.experiments.domain.Assignment

class AmplitudeTestGroupAssignmentTracker(amplitudeAdapter: AmplitudeAdapter) extends TestGroupAssignmentTracker {
  override def newAssignment(assignment: Assignment): Unit = {
    val event = AmplitudePetriEvent.fromAssignment(assignment)
    amplitudeAdapter.sendEvent(event)
  }
}

case class AmplitudePetriEvent(@JsonProperty("event_type") eventType: String,
                               ip: String, language: String, country: String,
                               @JsonProperty("user_id") userId: String,
                               @JsonProperty("event_properties") eventProperties: AmplitudePetriEventProperties) extends BaseAmplitudeEvent

case class AmplitudePetriEventProperties(experimentId: Int, url: String, productName: String, userAgent: String, testGroup: String)

object AmplitudePetriEvent {
  val petriBiEventType = "PetriBiEvent"

  def fromAssignment(assignment: Assignment): AmplitudePetriEvent = {
    val userInfo = assignment.getUserInfo
    AmplitudePetriEvent(
      eventType = s"${AmplitudePetriEvent.petriBiEventType}-${assignment.getExperimentId}",
      language = userInfo.language,
      ip = userInfo.ip,
      country = userInfo.country,
      userId = Option(userInfo.getUserId).getOrElse(UUID.fromString("00000000-0000-0000-0000-000000000000")).toString,
      eventProperties = AmplitudePetriEventProperties(
        experimentId = assignment.getExperimentId,
        url = userInfo.url,
        productName = assignment.getScope,
        userAgent = URLEncoder.encode(userInfo.userAgent, "UTF-8"),
        testGroup = assignment.getTestGroup.getValue
      )
    )
  }
}