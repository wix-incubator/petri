package com.wixpress.petri.laboratory

import org.specs2.matcher.Matcher
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope
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

  import FilterParametersExtractorsConfigReader._

  "build config" should {

    "return full config of extractor config deserialized from yaml and converters map according to config" in new Context {

      val extractorConfig = (HeaderExtractionOption.optionName, someHeader)
      val languageConfig = Map(LanguageFilterParameter.paramName -> List(extractorConfig))

      val config = new FilterParametersExtractorsConfig(languageConfig)
      buildFullConfig(config).extractors must_== Map(LanguageFilterParameter -> List((HeaderExtractionOption, someHeader)))
    }

    "filter params extractors config doesn't include not related configs" in new Context {

      val extractorConfig = (HeaderExtractionOption.optionName, someHeader)
      val converterConfig = (CoverterConfigOption, someConverter1)

      val languageConfigWithConverters = Map(LanguageFilterParameter.paramName -> List(extractorConfig, converterConfig))

      val config = new FilterParametersExtractorsConfig(languageConfigWithConverters)

      buildFullConfig(config).extractors must_== Map(LanguageFilterParameter -> List((HeaderExtractionOption, someHeader)))
    }


    "instantiate custom converter properly according to config" in new Context {
      val config = new FilterParametersExtractorsConfig(Map(
        LanguageFilterParameter.paramName -> List((CoverterConfigOption, someConverter1))))
      buildFullConfig(config).converters must haveConverterOf[CustomConverter1](LanguageFilterParameter)
    }
    
    "instantiate custom converters per each of filter parameters" in new Context {
      val config = new FilterParametersExtractorsConfig(Map(
        LanguageFilterParameter.paramName -> List((CoverterConfigOption, someConverter1)),
        CountryFilterParameter.paramName -> List((CoverterConfigOption, someConverter1)),
        UserIdFilterParameter.paramName -> List((CoverterConfigOption, someConverter2))))

      val converters = buildFullConfig(config).converters

      converters.size must_== 3
      converters must haveConverterOf[CustomConverter1](LanguageFilterParameter)
      converters must haveConverterOf[CustomConverter1](CountryFilterParameter)
      converters must haveConverterOf[CustomConverter2](UserIdFilterParameter)
    }
  }

  trait Context extends Scope {
    val someHeader = "KUKU_HEADER"
    val someConverter1 = classOf[CustomConverter1].getName
    val someConverter2 = classOf[CustomConverter2].getName

    def haveConverterOf[T <: Converter[_]: ClassTag](param: FilterParameter): Matcher[Map[FilterParameter, Converter[_]]] = {
      beSome(beAnInstanceOf[T]) ^^ { (_: Map[FilterParameter, Converter[_]]).get(param) aka "converter" }
    }
  }
}
