package com.wixpress.guineapig.services

import com.wixpress.guineapig.spi.SupportedLanguagesProvider

import scala.collection.JavaConversions._

class MockSupportedSupportedLanguagesProvider(supportedLanguages: java.util.Set[String])  extends SupportedLanguagesProvider {

  override def getSupportedLanguages: Set[String] = asScalaSet(supportedLanguages).toSet
}
