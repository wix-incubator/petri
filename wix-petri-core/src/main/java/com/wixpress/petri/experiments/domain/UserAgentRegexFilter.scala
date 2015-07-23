package com.wixpress.petri.experiments.domain

import com.fasterxml.jackson.annotation.JsonProperty

import scala.collection.JavaConversions._
import java.{util => ju}

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
case class UserAgentRegexFilter(@JsonProperty("includeRegexes") includeUserAgentRegexes: ju.List[String],
                                @JsonProperty("excludeRegexes") excludeUserAgentRegexes: ju.List[String]) extends Filter {

  override def isEligible(filterEligibility: EligibilityCriteria): Boolean = {
    includeUserAgentRegexes.isEmpty match {
      case true if excludeUserAgentRegexes.isEmpty => false
      case true => doesRegexNotExclude(filterEligibility.getUserAgent)
      case false if doesRegexInclude(filterEligibility.getUserAgent) => doesRegexNotExclude(filterEligibility.getUserAgent)
      case false => false

    }
  }

  private def doesRegexInclude(userAgent: String) = anElementInListMatchesRegex(userAgent, includeUserAgentRegexes)

  private def doesRegexNotExclude(userAgent: String) = !anElementInListMatchesRegex(userAgent, excludeUserAgentRegexes)

  private def anElementInListMatchesRegex(userAgent: String, list: ju.List[String]): Boolean =
    list.exists(userAgent.matches(_))


}
