package com.wixpress.petri.laboratory

import java.net.URLEncoder

import com.fasterxml.jackson.annotation.JsonProperty
import com.wixpress.petri.experiments.domain.Assignment
import com.wixpress.petri.utils.JsonMapper.asJson
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClientBuilder

class AmplitudeTestGroupAssignmentTracker(amplitudeAdapter: AmplitudeAdapter) extends TestGroupAssignmentTracker {
  override def newAssignment(assignment: Assignment): Unit = {
    val event = AmplitudePetriEvent.fromAssignment(assignment)
    amplitudeAdapter.sendEvent(event)
  }
}

class AmplitudeAdapter(amplitudeUrl: String, apiKey: String) {
  val client = HttpClientBuilder.create.build

  def sendEvent(event: BaseAmplitudeEvent): Unit = {
    val httpPost = new HttpPost(amplitudeUrl)
    val bodyStr = s"api_key=$apiKey&event=${asJson(event)}"
    val entity = new ByteArrayEntity(bodyStr.getBytes("UTF-8"))
    httpPost.setEntity(entity)

    val response = client.execute(httpPost)
    val resultCode = response.getStatusLine.getStatusCode

    if (resultCode != 200) {
      throw new FailedToPostAmplitudeEventException(resultCode)
    }
  }
}

trait BaseAmplitudeEvent {
  def eventType: String

  def userId: String
}

case class AmplitudeEvent(@JsonProperty("event_type") eventType: String,
                          ip: String, language: String, country: String,
                          @JsonProperty("user_id") userId: String) extends BaseAmplitudeEvent

case class AmplitudePetriEvent(@JsonProperty("event_type") eventType: String,
                               ip: String, language: String, country: String,
                               @JsonProperty("user_id") userId: String,
                               @JsonProperty("event_properties") eventProperties: AmplitudePetriEventProperties) extends BaseAmplitudeEvent


case class AmplitudePetriEventProperties(experimentId: Int, url: String, productName: String, userAgent: String, testGroup: Int)

object AmplitudePetriEvent {
  val petriBiEventType = "PetriBiEvent"

  def fromAssignment(assignment: Assignment): AmplitudePetriEvent = {
    val userInfo = assignment.getUserInfo
    AmplitudePetriEvent(
      eventType = AmplitudePetriEvent.petriBiEventType,
      language = userInfo.language,
      ip = userInfo.ip,
      country = userInfo.country,
      userId = userInfo.getUserId.toString,
      eventProperties = AmplitudePetriEventProperties(
        experimentId = assignment.getExperimentId,
        url = userInfo.url,
        productName = assignment.getScope,
        userAgent = URLEncoder.encode(userInfo.userAgent, "UTF-8"),
        testGroup = assignment.getTestGroup.getId
      )
    )
  }
}

class FailedToPostAmplitudeEventException(resultCode: Int) extends Exception(s"Failed to post bi event to amplitude - returned code $resultCode")