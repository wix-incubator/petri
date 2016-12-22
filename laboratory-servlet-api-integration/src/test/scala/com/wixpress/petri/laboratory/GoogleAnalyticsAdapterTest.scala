package com.wixpress.petri.laboratory

import java.util.UUID

import com.wixpress.petri.google_analytics.GoogleAnalyticsAdapter
import com.wixpress.petri.google_analytics.GoogleAnalyticsAdapterBuilder._
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

class GoogleAnalyticsAdapterTest extends SpecificationWithJUnit {

  "GoogleAnalyticsAdapter" should {
    "convert PetriBiEvent to google analytics body String" in new Context {
      googleAnalyticsAdapter.getBodyString(createBiPetriEvent()) must_==
        "v=1&tid=UA-89204848-1&t=event&el=PetriBiEvent-123&uip=THE_IP&ul=THE_LANGUAGE&geoid=THE_COUNTRY" +
          s"&cid=$userGuid&uid=$userGuid&ec=PRODUCT_NAME&ea=red&ua=THE_UA&dl=THE_URL"
    }

    "convert custom Bi event to google analytics body String when fields have custom names" in new Context {
      googleAnalyticsAdapter.getBodyString(createCustomBiEvent()) must_==
        s"v=1&tid=UA-89204848-1&t=event&el=customBiEvent&ul=en&geoid=UK&cid=$userGuid&uid=$userGuid"
    }

    "convert custom Bi event to google analytics body String when fields have google property names" in new Context {
      googleAnalyticsAdapter.getBodyString(createCustomGooglePropertyBiEvent()) must_==
        s"v=1&tid=UA-89204848-1&t=event&el=customBiEvent&ul=en&geoid=UK&cid=$userGuid&uid=$userGuid"
    }
  }

  class Context extends Scope {
    val googleAnalyticsAdapter = GoogleAnalyticsAdapter(defaultUrl, defaultTrackingId, defaultTimeout)
    val userGuid = UUID.randomUUID().toString

    def createBiPetriEvent(): BiPetriEvent = {
      BiPetriEvent(
        eventType = s"${BiPetriEvent.petriBiEventType}-123",
        language = "THE_LANGUAGE",
        country = "THE_COUNTRY",
        ip = "THE_IP",
        userId = userGuid,
        eventProperties = PetriEventProperties(
          experimentId = 123,
          productName = "PRODUCT_NAME",
          url = "THE_URL",
          userAgent = "THE_UA",
          testGroup = "red"
        )
      )
    }

    def createCustomBiEvent(): BaseBiEvent = {
      new BaseBiEvent {
        val eventType = "customBiEvent"
        val language = "en"
        val country = "UK"
        val userId = userGuid
      }
    }

    def createCustomGooglePropertyBiEvent(): BaseBiEvent = {
      new BaseBiEvent {
        val eventType = "customBiEvent"
        val ul = "en"
        val geoid = "UK"
        val userId = userGuid
      }
    }
  }
}
