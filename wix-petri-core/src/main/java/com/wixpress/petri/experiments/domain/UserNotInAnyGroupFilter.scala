package com.wixpress.petri.experiments.domain

import java.util.UUID
import java.{util => ju}

/**
 * User: Dalias
 * Date: 9/16/14
 * Time: 9:48 AM
 */
case class UserNotInAnyGroupFilter(excludeUserGroups: ju.List[String]) extends Filter {
  override def isEligible(filterEligibility: EligibilityCriteria): Boolean = {
    val userGroupsService = filterEligibility.getExternalDataFetchers.userGroupsService
    !filterEligibility.isCompanyEmployee ||
      !userGroupsService.isUserInAnyGroup(filterEligibility.getUserId, excludeUserGroups)

  }

}

trait UserGroupsService {
  def isUserInAnyGroup(userId: UUID, groups: ju.List[String]) : Boolean
}

