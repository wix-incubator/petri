package com.wixpress.petri.laboratory

import java.util.UUID

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
import org.springframework.mock.web.MockHttpServletRequest

class UserIdResolverTest extends SpecificationWithJUnit {

  trait Context extends Scope {
    val request = new MockHttpServletRequest
    val config = FilterParametersConfig()
  }

  "UserIdResolver" should {
    "resolve id by 'laboratory_user_id' url param when empty FilterParametersExtractorsConfig" in new Context {
      private val randomUUID = UUID.randomUUID()
      request.addParameter("laboratory_user_id", randomUUID.toString)
      UserIdResolver.resolve(request, config) must be equalTo randomUUID
    }

    "resolve id to null when empty FilterParametersExtractorsConfig and 'laboratory_user_id' is missing" in new Context {
      UserIdResolver.resolve(request, config) must beNull
    }
  }
}
