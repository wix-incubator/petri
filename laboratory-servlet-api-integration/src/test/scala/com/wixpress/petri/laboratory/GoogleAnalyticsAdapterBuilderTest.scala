package com.wixpress.petri.laboratory

import com.wixpress.petri.google_analytics.GoogleAnalyticsAdapterBuilder._
import com.wixpress.petri.google_analytics.{GoogleAnalyticsAdapter, GoogleAnalyticsAdapterBuilder}
import org.specs2.mutable.SpecificationWithJUnit

class GoogleAnalyticsAdapterBuilderTest extends SpecificationWithJUnit {
  "GoogleAnalyticsAdapterBuilder" should {
    "be created from default values if no configuration values are present" in {
      GoogleAnalyticsAdapterBuilder.create(null, null, null) === GoogleAnalyticsAdapter(defaultUrl, defaultTrackingId, defaultTimeout)
    }

    "be created from default timeout if supplied with an invalid int" in {
      GoogleAnalyticsAdapterBuilder.create("url", "trackingId", "invalidTimeout") === GoogleAnalyticsAdapter("url", "trackingId", defaultTimeout)
    }

    "be created from configuration values" in {
      GoogleAnalyticsAdapterBuilder.create("url", "trackingId", "10000") === GoogleAnalyticsAdapter("url", "trackingId", 10000)
    }
  }
}
