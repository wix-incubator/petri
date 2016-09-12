package com.wixpress.guineapig.dto

case class UserAgentRegex(regex: String, description: String) extends MetaData {
  override def matchesId(id: String): Boolean = regex == id
}
