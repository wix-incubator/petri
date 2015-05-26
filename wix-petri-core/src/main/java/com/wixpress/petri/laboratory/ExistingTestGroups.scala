package com.wixpress.petri.laboratory

import java.{util => ju}

/**
 * User: Dalias
 * Date: 2/22/15
 * Time: 5:20 PM
 */
case class ExistingTestGroups( testGroupsFromCookies: ju.Map[String,String],  testGroupsFromServer: ju.Map[String,String]) {

  def get(experimentId: Int) : Option[Integer] = {
    Option(testGroupsFromCookies.get(experimentId.toString)) match {
      case None => Option(testGroupsFromServer.get(experimentId.toString)).map(_.toInt)
      case Some(testGroupId) => Some(testGroupId.toInt)
    }
  }
}
