package com.wixpress.petri.test

import java.util.Optional

import com.wixpress.petri.test.SampleAppRunner.DEFAULT_PATH_TO_WEBAPP
import org.specs2.mutable.SpecificationWithJUnit

import scala.io.Source.fromFile

class LaboratoryPropertiesIT extends SpecificationWithJUnit {

  "laboratory.properties file" should {
    "remain the same as before instantiating SampleAppRunner" in {
      val reporterIntervalDifferentVal = 7

      val originalLabPropsContent = labPropFileContent
      val sampleApp = new SampleAppRunner(9015, DEFAULT_PATH_TO_WEBAPP, reporterIntervalDifferentVal, false, Optional.empty())

      sampleApp.start()
      sampleApp.stop()

      labPropFileContent mustEqual originalLabPropsContent
    }
  }

  def labPropFileContent = fromFile(s"$DEFAULT_PATH_TO_WEBAPP/WEB-INF/laboratory.properties").mkString
}
