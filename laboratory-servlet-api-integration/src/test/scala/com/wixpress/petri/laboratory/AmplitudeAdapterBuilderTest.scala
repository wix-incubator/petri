package com.wixpress.petri.laboratory

import com.wixpress.petri.amplitude.{AmplitudeAdapter, AmplitudeAdapterBuilder}
import com.wixpress.petri.amplitude.AmplitudeAdapterBuilder._
import org.specs2.mutable.SpecificationWithJUnit

class AmplitudeAdapterBuilderTest extends SpecificationWithJUnit {
  "AmplitudeAdapterBuilder" should {
    "be created from default values if no configuration values are present" in {
      AmplitudeAdapterBuilder.create(null, null, null) === AmplitudeAdapter(defaultUrl, defaultApiKey, defaultTimeout)
    }

    "be created from default timeout if supplied with an invalid int" in {
      AmplitudeAdapterBuilder.create("url", "apiKey", "invalidTimeout") === AmplitudeAdapter("url", "apiKey", defaultTimeout)
    }

    "be created from configuration values" in {
      AmplitudeAdapterBuilder.create("url", "apiKey", "10000") === AmplitudeAdapter("url", "apiKey", 10000)
    }
  }
}
