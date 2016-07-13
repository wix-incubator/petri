package com.wixpress.petri.laboratory

import javax.servlet.http.Cookie

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
import org.springframework.mock.web.MockHttpServletRequest

class CountryResolverTest extends SpecificationWithJUnit{

  trait Context extends Scope {
    val resolver = CountryResolver()
    val request = new MockHttpServletRequest
  }

  "CountryResolver" should {
    "resolve by default servlet default locale" in new Context {
      val config = FilterParametersExtractorsConfig()

      resolver.resolve(request, config) must beEqualTo(request.getLocale.getCountry)
    }

    "resolve by 'GEOIP_COUNTRY_CODE' header if filterParamConfig is empty" in new Context {
      val config = FilterParametersExtractorsConfig()

      private val country =  "SomeCountry"
      request.addHeader("GEOIP_COUNTRY_CODE", country)
      resolver.resolve(request, config) must beEqualTo(country)
    }

    "resolve by 'SOME_HEADER_NAME' header if filterParamConfig defines filterParam Country header" in new Context {
      val config = new FilterParametersExtractorsConfig(
        Map("Country" -> List(("Header", "SOME_HEADER_NAME"))))

      private val country =  "SomeCountry"
      request.addHeader("SOME_HEADER_NAME", country)
      resolver.resolve(request, config) must beEqualTo(country)
    }

    "resolve by 'SOME_COOKIE_NAME' cookie if filterParamConfig defines filterParam Country cookie" in new Context {
      val config = new FilterParametersExtractorsConfig(
        Map("Country" -> List(("Cookie", "SOME_COOKIE_NAME"))))

      private val country =  "SomeCountry"
      request.setCookies(new Cookie("SOME_COOKIE_NAME", country))
      resolver.resolve(request, config) must beEqualTo(country)
    }

    "resolve by 'SOME_PARAM_NAME' cookie if filterParamConfig defines filterParam Country param" in new Context {
      val config = new FilterParametersExtractorsConfig(
        Map("Country" -> List(("Param", "SOME_PARAM_NAME"))))

      private val country =  "SomeCountry"
      request.addParameter("SOME_PARAM_NAME", country)
      resolver.resolve(request, config) must beEqualTo(country)
    }


    "resolve by configuration order if first config is missing move to next" in new Context {
      val config = new FilterParametersExtractorsConfig(
        Map("Country" -> List(("Cookie", "SOME_COOKIE_NAME"), ("Header", "Some_Header"))))

      private val country =  "SomeCountry"
      request.addHeader("Some_Header", country)
      resolver.resolve(request, config) must beEqualTo(country)
    }

  }
}