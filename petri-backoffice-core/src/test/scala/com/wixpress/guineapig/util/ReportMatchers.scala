package com.wixpress.guineapig.util

import com.wixpress.guineapig.entities.ui.ExperimentReport
import org.hamcrest.{Description, Matcher, TypeSafeMatcher}
import org.joda.time.DateTime._

import scala.collection.JavaConversions._

/**
 * Created by talyas on 2/16/15.
 */

object ReportMatchers {

  def hasOneCountForValue(experimentValue: String): Matcher[ExperimentReport] = {
    new TypeSafeMatcher[ExperimentReport]() {
      protected def matchesSafely(item: ExperimentReport): Boolean = {

        item.totalCount == 1 &&
        item.fiveMinuteCount == 1 &&
        item.lastUpdated.isAfter(now.minusMinutes(5)) &&
        item.reportsPerValue.size == 1 &&
        item.reportsPerValue.get(0).experimentValue == experimentValue &&
        item.reportsPerValue.get(0).totalCount == 1
      }

      def describeTo(description: Description) {
        description.appendText("ExperimentReport contain experimentValue: " + experimentValue + "count: " + 1)
      }
    }
  }


}
