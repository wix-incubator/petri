package com.wixpress.petri.test

import com.wixpress.petri.test.SampleAppRunner.DEFAULT_PATH_TO_WEBAPP
import org.specs2.mutable.SpecificationWithJUnit

class LaboratoryPropertiesIT extends SpecificationWithJUnit {

  "laboratory.properties file" should {
    "be resilient to IT tests (remain the same as before instantiating SampleAppRunner)" in {
      val reporterIntervalDifferentVal = 7
      def labPropFileContent = scala.io.Source.fromFile(DEFAULT_PATH_TO_WEBAPP
        + "/WEB-INF/laboratory.properties").mkString

      val originalLabPropsContent = labPropFileContent
      val sampleApp = new SampleAppRunner(9015, DEFAULT_PATH_TO_WEBAPP, reporterIntervalDifferentVal, false)

      sampleApp.start()
      sampleApp.stop()

      labPropFileContent mustEqual originalLabPropsContent
    }
  }
}
