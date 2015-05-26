package com.wixpress.petri.util

import com.wixpress.petri.petri.ConductExperimentSummary
import org.hamcrest.{Description, Matcher, TypeSafeMatcher}

/**
 * User: Dalias
 * Date: 1/6/15
 * Time: 2:51 PM
 */
object ConductExperimentSummaryMatcher {

  def hasSummary(serverName: String, experimentId: Int, experimentValue: String, count: Long): Matcher[ConductExperimentSummary] = {
    new TypeSafeMatcher[ConductExperimentSummary]()  {
      protected def matchesSafely(item: ConductExperimentSummary): Boolean ={
        item.experimentId == experimentId && (item.experimentValue == experimentValue) && item.fiveMinuteCount == count &&
          item.totalCount == count && item.serverName.equals(serverName)
      }
      def describeTo(description: Description) {
        description.appendText("ConductExperimentSummary contain experimentId: " + experimentId + "experimentValue: " + experimentValue + "count: " + count)
      }
    }
  }

}
