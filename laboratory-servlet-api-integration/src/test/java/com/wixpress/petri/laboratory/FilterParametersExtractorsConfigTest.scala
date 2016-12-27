package com.wixpress.petri.laboratory

import org.specs2.matcher.Matcher
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
import com.wixpress.petri.laboratory.FilterParameters._
import com.wixpress.petri.laboratory.FilterParametersConfigOptions.Converter
import scala.reflect.ClassTag

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

  import FilterParametersExtractorsConfig._

  "instantiate converters" should {
    "instantiate custom converter properly according to config" in new Context {
      val config = new FilterParametersExtractorsConfig(Map(
        Language.toString -> List((Converter.toString, someConverter1))))
      instantiateConverters(config).converters must haveConverterOf[CustomConverter1](Language.toString)
    }
    
    "instantiate custom converters per each of filter parameters" in new Context {
      val config = new FilterParametersExtractorsConfig(Map(
        Language.toString -> List((Converter.toString, someConverter1)),
        Country.toString -> List((Converter.toString, someConverter1)),
        UserId.toString -> List((Converter.toString, someConverter2))))

      val converters = instantiateConverters(config).converters

      converters.size must_== 3
      converters must haveConverterOf[CustomConverter1](Language.toString)
      converters must haveConverterOf[CustomConverter1](Country.toString)
      converters must haveConverterOf[CustomConverter2](UserId.toString)
    }
  }

  trait Context extends Scope {
    val someConverter1 = classOf[CustomConverter1].getName
    val someConverter2 = classOf[CustomConverter2].getName

    def haveConverterOf[T <: Converter[_]: ClassTag](key: String): Matcher[Map[String, Converter[_]]] = {
      beSome(beAnInstanceOf[T]) ^^ { (_: Map[String, Converter[_]]).get(key) aka "converter" }
    }
  }
}
