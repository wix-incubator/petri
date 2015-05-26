package com.wixpress.petri.petri

import java.sql.ResultSet
import java.util.UUID

import org.joda.time.DateTime
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.{JdbcTemplate, RowMapper}

/**
 * User: Dalias
 * Date: 12/10/14
 * Time: 4:30 PM
 */
trait UserStateDao {
  def getUserState( userId: UUID): String
  def getFullUserState( userId: UUID): UserState

  def saveUserState(userId: UUID, state: String, currentDateTime: DateTime)

}

class JdbcUserStateDao(jdbcTemplate: JdbcTemplate) extends UserStateDao {

  private val insertUserStateQuery = "INSERT INTO userState (user_id, state, date_updated) VALUES (?,?, ?) "
  private val updateUserStateQuery = "UPDATE userState SET state = ? , date_updated = ? WHERE user_id = ?"
  private val getUserStateQuery = "SELECT state FROM userState WHERE user_id = ?"
  private val getFullUserStateQuery = "SELECT * FROM userState WHERE user_id = ?"
  private val userStateMapper = new UserStateMapper
  private val fullUserStateMapper = new FullUserStateMapper

  override def  getUserState( userId: UUID): String = {
     try {
       jdbcTemplate.queryForObject(getUserStateQuery, userStateMapper, userId.toString)
     } catch {
       case empty: EmptyResultDataAccessException => ""
       case e: Exception => throw e;
     }
  }

  override def getFullUserState(userId: UUID): UserState = {
    jdbcTemplate.queryForObject(getFullUserStateQuery, fullUserStateMapper, userId.toString)
  }

  override def saveUserState(userId: UUID, state: String, currentDateTime: DateTime): Unit = {
    val now = asLong(currentDateTime.getMillis)
    val numOfResults: Int = jdbcTemplate.update(updateUserStateQuery,  state, now, userId.toString)
    if(numOfResults == 0)
      jdbcTemplate.update(insertUserStateQuery,  userId.toString ,state, now)
  }




  class UserStateMapper extends RowMapper[String] {
    override def mapRow(rs: ResultSet, rowNum: Int): String =  rs.getString("state")
  }

  class FullUserStateMapper extends RowMapper[UserState] {
    override def mapRow(rs: ResultSet, rowNum: Int): UserState =  new UserState(UUID.fromString(rs.getString("user_id")), rs.getString("state"), new DateTime(rs.getLong("date_updated")))
  }

  private def asLong(num : Long) : java.lang.Long = num

}

