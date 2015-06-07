package com.wixpress.petri.laboratory

import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.mock.Mockito
import com.wixpress.petri.petri.SpecDefinition
import org.specs2.specification.Scope
import com.wixpress.petri.laboratory.ExperimentOutcome.UndefinedFallback

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 2/16/14
 */
class RichLaboratoryTest extends SpecificationWithJUnit with Mockito {
 
  trait ctx extends Scope {
    val laboratory = mock[Laboratory]

    laboratory.conductExperiment(===(classOf[MySpec]), any, any[TestResultConverter[Any]]) answers { (args, _) =>
      val converter = args.asInstanceOf[Array[Any]].apply(2).asInstanceOf[TestResultConverter[Any]]
      converter.convert("777")
    }
  }

  "func1ToTestResultConverter" should {

    "convert Function1 to TestResultConverter" in new ctx {
      laboratory.conductExperiment(classOf[MySpec], 666, (_: String).toInt) === 777
    }
  }
  
  "getOrElse" should {

    "return fallback" in new ctx {
      laboratory.conductExperiment(classOf[MySpec], "fallback") returns "fallback"

      laboratory.conduct[MySpec].getOrElse("fallback") must be_===("fallback")
    }

    "return conducted group" in new ctx {
      laboratory.conductExperiment(classOf[MySpec], "fallback") returns "group"

      laboratory.conduct[MySpec].getOrElse("fallback") must be_===("group")
    }
  }
  
  "fold" should {

    "fallback when not defined" in new ctx {
      laboratory.conductExperiment(===(classOf[MySpec]), any) returns "fallback"
      
      val x: Unit => Int = laboratory.conduct[MySpec].fold(1) {
        case "x" => 2
      }
      
      x((): Unit) must be_===(1)
    }

    "fallback for complete function" in new ctx {
      laboratory.conductExperiment(===(classOf[MySpec]), any) returns UndefinedFallback

      val x: Unit => Int = laboratory.conduct[MySpec].fold(1) {
        case _ => 2
      }

      x((): Unit) must be_===(1)
    }

    "execute defined branch for complete function" in new ctx {
      laboratory.conductExperiment(===(classOf[MySpec]), any) returns "some-group"

      val x: Unit => Int = laboratory.conduct[MySpec].fold(1) {
        case _ => 2
      }

      x((): Unit) must be_===(2)
    }

    "execute defined branch" in new ctx {

      laboratory.conductExperiment(===(classOf[MySpec]), any) returns "x"

      val x: Unit => Int = laboratory.conduct[MySpec].fold(1) {
        case "x" => 2
      }

      x((): Unit) must be_===(2)
    }

    "explode with exception" in new ctx {
      laboratory.conductExperiment(===(classOf[MySpec]), any) returns "x"

      val x: Unit => Int = laboratory.conduct[MySpec].fold(1) {
        case "x" => throw new RuntimeException
      }

      x((): Unit) must throwA[RuntimeException]
    }
  }
}

class MySpec extends SpecDefinition
