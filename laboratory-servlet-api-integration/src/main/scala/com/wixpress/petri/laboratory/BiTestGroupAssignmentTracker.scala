package com.wixpress.petri.laboratory

import java.net.URLEncoder
import java.util.UUID

import com.fasterxml.jackson.annotation.JsonProperty
import com.wixpress.petri.experiments.domain.Assignment

class BiTestGroupAssignmentTracker(adapter: BiAdapter) extends TestGroupAssignmentTracker {
  override def newAssignment(assignment: Assignment): Unit = {
    val event = BiPetriEvent.fromAssignment(assignment)
    try {
      adapter.sendEvent(event)
    } catch {
      case e: Throwable =>
        println(s"Failed to write BI Event to: ${adapter.getClass.getName} for experiment ${assignment.getExperimentId}")
        e.printStackTrace()
    }
  }
}

//TODO - the jsonProperties here are to match amplitude request syntax. should be extracted into the AmplitudeAdapter..
case class BiPetriEvent(@JsonProperty("event_type") eventType: String,
                        ip: String, language: String, country: String,
                        @JsonProperty("user_id") userId: String,
                        @JsonProperty("event_properties") eventProperties: PetriEventProperties) extends BaseBiEvent

case class PetriEventProperties(experimentId: Int, url: String, productName: String, userAgent: String, testGroup: String)

object BiPetriEvent {
  val petriBiEventType = "PetriBiEvent"

  def fromAssignment(assignment: Assignment): BiPetriEvent = {
    val userInfo = assignment.getUserInfo
    BiPetriEvent(
      eventType = s"${BiPetriEvent.petriBiEventType}-${assignment.getExperimentId}",
      language = userInfo.language,
      ip = userInfo.ip,
      country = userInfo.country,
      userId = Option(userInfo.getUserId).getOrElse(UUID.fromString("00000000-0000-0000-0000-000000000000")).toString,
      eventProperties = PetriEventProperties(
        experimentId = assignment.getExperimentId,
        url = userInfo.url,
        productName = assignment.getScope,
        userAgent = URLEncoder.encode(userInfo.userAgent, "UTF-8"),
        testGroup = assignment.getTestGroup.getValue
      )
    )
  }
}
