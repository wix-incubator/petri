package com.wixpress.petri.petri

import java.util.UUID

import org.joda.time.DateTime

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
case class UserState(userId: UUID, state: String, updateDate: DateTime)
