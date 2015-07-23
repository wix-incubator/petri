package com.wixpress.petri.petri

import javax.mail.internet.InternetAddress

import scala.collection.JavaConverters._

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
case class MailRecipients(to: Set[InternetAddress], cc: Set[InternetAddress])  {
  def this(javaTO: java.util.Set[InternetAddress], javaCC: java.util.Set[InternetAddress]) = {
    this(javaTO.asScala.toSet, javaCC.asScala.toSet)
  }
}
