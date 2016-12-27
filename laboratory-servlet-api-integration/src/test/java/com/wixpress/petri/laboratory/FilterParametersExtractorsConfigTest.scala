package com.wixpress.petri.laboratory

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

/**
 * Created by litalt on 27/12/16.
 */

class CustomConverter1 extends Converter[String] {
  def convert(value: String): String = value
}
class CustomConverter2 extends Converter[String] {
  def convert(value: String): String = value
}

class FilterParametersExtractorsConfigTest extends SpecificationWithJUnit {

  import com.wixpress.petri.laboratory.FilterParametersExtractorsConfig._
  trait Context extends Scope {
    val someConverter1 = classOf[CustomConverter1].getName
    val someConverter2 = classOf[CustomConverter2].getName
  }

  "instantiate converters" should {
    "instantiate custom converter properly according to config" in new Context {
      val config = new FilterParametersExtractorsConfig(Map(
        FilterParameters.Language.toString -> List((FilterParametersConfigOptions.Converter.toString, someConverter1))))
      instantiateConverters(config).converters.get(FilterParameters.Language.toString).get must beAnInstanceOf[CustomConverter1]
    }
    
    "instantiate custom converters per each of filter parameters" in new Context {
      val config = new FilterParametersExtractorsConfig(Map(
        FilterParameters.Language.toString -> List((FilterParametersConfigOptions.Converter.toString, someConverter1)),
        FilterParameters.Country.toString -> List((FilterParametersConfigOptions.Converter.toString, someConverter1)),
        FilterParameters.UserId.toString -> List((FilterParametersConfigOptions.Converter.toString, someConverter2))))

      val converters = instantiateConverters(config).converters
      converters.size must_== 3
      converters.get(FilterParameters.Language.toString).get must beAnInstanceOf[CustomConverter1]
      converters.get(FilterParameters.Country.toString).get must beAnInstanceOf[CustomConverter1]
      converters.get(FilterParameters.UserId.toString).get must beAnInstanceOf[CustomConverter2]
    }
  }
}
