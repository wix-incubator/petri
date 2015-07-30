package com.wixpress.petri.petri

import java.util.UUID

import org.joda.time.DateTime

/**
 * User: Dalias
 * Date: 3/22/15
 * Time: 11:52 AM
 */
case class UserState(userId: UUID, state: String, updateDate: DateTime)
