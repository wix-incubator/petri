package com.wixpress.petri.petri

import java.util.UUID

import _root_.util.DBDriver
import org.joda.time.DateTime
import org.specs2.matcher.Scope
import org.specs2.mutable.SpecificationWithJUnit

/**
 * User: Dalias
 * Date: 12/10/14
 * Time: 4:45 PM
 */
class JdbcUserStateDaoIT extends SpecificationWithJUnit  {
  sequential

  val JDBC_H2_IN_MEM_CONNECTION_STRING: String = "jdbc:h2:mem:test"

  trait Context extends Scope{
    val dbDriver = DBDriver.dbDriver(JDBC_H2_IN_MEM_CONNECTION_STRING)
    val userState = new JdbcUserStateDao(dbDriver.jdbcTemplate)
    dbDriver.createSchema()
    val userGuid: UUID = UUID.randomUUID
    val originalUserState = "1#5"
    val currentDateTime = new DateTime()


  }

  "User State Dao " should {
     "save a single user state successfully" in new Context  {

       userState.saveUserState (userGuid, originalUserState, currentDateTime)

       userState.getUserState(userGuid) must_== originalUserState
     }


    "save a user state on user ID that already exists overrides it " in new Context  {

      userState.saveUserState(userGuid, originalUserState, currentDateTime)
      val updatesUserState = originalUserState + "|3#7"

      userState.saveUserState(userGuid, updatesUserState, currentDateTime)
      userState.getUserState(userGuid) must_== updatesUserState


    }

    "Retrieving a user state that doesn't exists returns None" in new Context  {

      userState.getUserState(userGuid) must_== ""


    }

    "Retrieving a full user state successfully after initial save" in new Context  {
      userState.saveUserState (userGuid, originalUserState, currentDateTime)

      userState.getFullUserState(userGuid) must_== UserState(userGuid, originalUserState, currentDateTime)

    }

    "Retrieving a full user state successfully after update" in new Context  {
      userState.saveUserState (userGuid, originalUserState, currentDateTime)
      val updatesUserState = originalUserState + "|3#7"
       val newDateTime = new DateTime()
      userState.saveUserState(userGuid, updatesUserState, newDateTime)

      userState.getFullUserState(userGuid) must_== UserState(userGuid, updatesUserState, newDateTime)


    }

  }

}
