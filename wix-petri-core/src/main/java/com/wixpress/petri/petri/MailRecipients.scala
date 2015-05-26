package com.wixpress.petri.petri

import javax.mail.internet.InternetAddress

import scala.collection.JavaConverters._

/**
 * Created by Nimrod_Lahav on 4/27/15.
 */
case class MailRecipients(to: Set[InternetAddress], cc: Set[InternetAddress])  {
  def this(javaTO: java.util.Set[InternetAddress], javaCC: java.util.Set[InternetAddress]) = {
    this(javaTO.asScala.toSet, javaCC.asScala.toSet)
  }
}
