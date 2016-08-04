package com.wixpress.petri.laboratory

import com.wixpress.petri.amplitude.AmplitudeAdapter
import com.wixpress.petri.amplitude.AmplitudeAdapter._
import org.specs2.mutable.SpecificationWithJUnit

class AmplitudeAdapterTest extends SpecificationWithJUnit {
  "AmplitudeAdapter" should {
    "be created from default values if no configuration values are present" in {
      AmplitudeAdapter.create(null, null, null) === AmplitudeAdapter(defaultUrl, defaultApiKey, defaultTimeout)
    }

    "be created from default timeout if supplied with an invalid int" in {
      AmplitudeAdapter.create("url", "apiKey", "invalidTimeout") === AmplitudeAdapter("url", "apiKey", defaultTimeout)
    }

    "be created from configuration values" in {
      AmplitudeAdapter.create("url", "apiKey", "10000") === AmplitudeAdapter("url", "apiKey", 10000)
    }
  }
}
