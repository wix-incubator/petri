package com.wixpress.petri.experiments.domain

import java.util.UUID

import com.natpryce.makeiteasy.MakeItEasy
import com.natpryce.makeiteasy.MakeItEasy._
import com.wixpress.common.specs2.JMock
import com.wixpress.petri.experiments.domain.FilterTestUtils._
import com.wixpress.petri.laboratory.UserInfo
import com.wixpress.petri.laboratory.dsl.UserInfoMakers
import com.wixpress.petri.laboratory.dsl.UserInfoMakers._
import org.specs2.matcher.MustThrownExpectations
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

import scala.collection.JavaConversions._

/**
  * User: Dalias
  * Date: 1/11/16
  * Time: 12:11 PM
  */
class UserNotInAnyGroupFilterTest extends SpecificationWithJUnit {

  trait Context extends Scope with JMock with MustThrownExpectations {
    val userGroupsService = mock[UserGroupsService]
    val externalDataFetchers = ExternalDataFetchers(userGroupsService)
    val excludedGroup = List("someGroup2")
    val filter = UserNotInAnyGroupFilter(excludedGroup)

    val nonWixUserInfo: UserInfo = a(UserInfoMakers.UserInfo).make
    val wixUserInfo: UserInfo = a(UserInfoMakers.UserInfo, MakeItEasy.`with`(companyEmployee, true.asInstanceOf[java.lang.Boolean])).make

    def expectUserInGroups(userId: UUID, isExcluded: Boolean): Unit = {
        checking { allowing(userGroupsService).isUserInAnyGroup(userId, excludedGroup).willReturn(isExcluded) }
    }
  }

  "UserNotInAnyGroupFilter" should {

    "return true when not company user" in new Context {
      filter.isEligible(defaultEligibilityCriteriaForUser(nonWixUserInfo, externalDataFetchers)) must beTrue
    }

    "return true when company user and not in excluded group" in new Context {
      expectUserInGroups(wixUserInfo.getUserId, isExcluded = false)
      filter.isEligible(defaultEligibilityCriteriaForUser(wixUserInfo, externalDataFetchers)) should beTrue
    }

    "return false when company user and in excluded group" in new Context {
      expectUserInGroups(wixUserInfo.getUserId, isExcluded = true)
      filter.isEligible(defaultEligibilityCriteriaForUser(wixUserInfo, externalDataFetchers)) should beFalse
    }
  }
}
