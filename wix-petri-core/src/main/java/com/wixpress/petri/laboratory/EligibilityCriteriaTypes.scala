package com.wixpress.petri.laboratory

import org.joda.time.DateTime

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
object EligibilityCriteriaTypes {

  class UserCreationDateCriterion(creationDate: DateTime) extends EligibilityCriterion[DateTime] {
    override def getValue: DateTime = creationDate
  }

  class LanguageCriterion(language: String) extends EligibilityCriterion[String] {
    override def getValue: String = language
  }

  class CountryCriterion(geo: String) extends EligibilityCriterion[String] {
    override def getValue: String = geo
  }

  class CustomContextCriterion(customContextMap: java.util.Map[String,String]) extends EligibilityCriterion[java.util.Map[String,String]] {
    override def getValue: java.util.Map[String,String] = customContextMap
  }


}
