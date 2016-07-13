package com.wixpress.petri.laboratory

import javax.servlet.http.{Cookie, HttpServletRequest}

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
import org.springframework.mock.web.MockHttpServletRequest

class ResolversTest extends SpecificationWithJUnit {

  trait Context extends Scope {
    val aDefaultResolution = "default"
    val aFilterParam = FilterParameters.Language
    val someValue = "someVal"

    val resolver = new StringResolver {
      override def defaultResolution(request: HttpServletRequest): String = aDefaultResolution
      override val filterParam: FilterParameters.Value = aFilterParam
    }

    val request = new MockHttpServletRequest
  }


  "Resolver" should {
    "resolve by default resolution when configs empty" in new Context {
      val config = FilterParametersExtractorsConfig()
      resolver.resolve(request, config) must beEqualTo(aDefaultResolution)
    }

    "resolve by header 'SOME_HEADER' when configured" in new Context {
      val config = FilterParametersExtractorsConfig(Map(aFilterParam.toString -> List(("Header", "SOME_HEADER"))))

      request.addHeader("SOME_HEADER", someValue)

      resolver.resolve(request, config) must beEqualTo(someValue)
    }

    "resolve by 'SOME_COOKIE_NAME' cookie if filterParamConfig defines filterParam Country cookie" in new Context {
      val config = new FilterParametersExtractorsConfig(
        Map(aFilterParam.toString -> List(("Cookie", "SOME_COOKIE_NAME"))))

      request.setCookies(new Cookie("SOME_COOKIE_NAME", someValue))

      resolver.resolve(request, config) must beEqualTo(someValue)
    }

    "resolve by param 'SOME_PARAM' when configuration before that are not present" in new Context {
      val config = FilterParametersExtractorsConfig(
        Map(aFilterParam.toString ->
          List(("Header", "SOME_HEADER"),
            ("Cookie", "SOME_COOKIE"),
            ("Param", "SOME_PARAM"))))

      request.addParameter("SOME_PARAM", someValue)

      resolver.resolve(request, config) must beEqualTo(someValue)
    }

    "resolve by configuration order if first config is missing move to next" in new Context {
      val config = new FilterParametersExtractorsConfig(
        Map(aFilterParam.toString ->
          List(("Cookie", "SOME_COOKIE_NAME"), ("Header", "Some_Header"))))

      request.addHeader("Some_Header", someValue)

      resolver.resolve(request, config) must beEqualTo(someValue)
    }

    "resolve by default behaviour when configuration before that are not present" in new Context {
      val config = FilterParametersExtractorsConfig(
        Map(aFilterParam.toString ->
          List(("Header", "SOME_HEADER"),
            ("Cookie", "SOME_COOKIE"),
            ("Param", "SOME_PARAM"))))

      resolver.resolve(request, config) must beEqualTo(aDefaultResolution)
    }

    "resolve by first applicable configuration -> Param" in new Context {
      val config = new FilterParametersExtractorsConfig(
        Map(aFilterParam.toString ->
          List(("Cookie", "SOME_COOKIE_NAME"), ("Param", "Some_Param"), ("Header", "Some_Header"))))

      request.addHeader("Some_Header", "someOtherValue")
      request.addParameter("Some_Param", someValue)

      resolver.resolve(request, config) must beEqualTo(someValue)
    }

    "be able to resolve different types than String" in {
      val aFilterParam = FilterParameters.Language
      val request = new MockHttpServletRequest

      val resolver = new Resolver[Int] {
        override val filterParam: FilterParameters.Value = aFilterParam
        override def convert(value: String): Int = Integer.parseInt(value)
        override def defaultResolution(request: HttpServletRequest): Int = 1
      }

      val config = FilterParametersExtractorsConfig(Map(aFilterParam.toString -> List(("Header", "SOME_HEADER"))))
      request.addHeader("SOME_HEADER", "999")

      resolver.resolve(request, config) must beEqualTo(999)
    }
  }
}