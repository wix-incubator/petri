package com.wixpress.petri.laboratory

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
import org.springframework.mock.web.MockHttpServletRequest

class CountryResolverTest extends SpecificationWithJUnit{

  trait Context extends Scope {
    val resolver = CountryResolver()
    val request = new MockHttpServletRequest
    val config = FilterParametersConfig()
  }

  "CountryResolver" should {
    "resolve by default servlet default locale" in new Context {
      resolver.resolve(request, config) must beEqualTo(request.getLocale.getCountry)
    }

    "resolve by 'GEOIP_COUNTRY_CODE' header if filterParamConfig is empty" in new Context {

      private val country =  "SomeCountry"
      request.addHeader("GEOIP_COUNTRY_CODE", country)
      resolver.resolve(request, config) must beEqualTo(country)
    }
  }
}