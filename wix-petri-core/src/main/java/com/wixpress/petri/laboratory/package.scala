package com.wixpress.petri

import scala.reflect.ClassTag
import scala.language.implicitConversions

import com.wixpress.petri.petri.SpecDefinition


/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
package object laboratory {

  implicit def func1ToTestResultConverter[U](f: String => U): TestResultConverter[U] = new TestResultConverter[U] {
    def convert(value: String): U = f(value)
  }

  implicit class RichLaboratory(val laboratory: Laboratory) extends AnyVal {
    def conduct[T <: SpecDefinition : ClassTag]: ExperimentOutcome = new ExperimentOutcome {
      def conduct(fallback: String): String = laboratory.conductExperiment(experimentClass, fallback)
    }

    def conductWithContext[T <: SpecDefinition : ClassTag](ctx: ConductionContext): ExperimentOutcome = new ExperimentOutcome {
      def conduct(fallback: String): String = laboratory.conductExperiment(experimentClass, fallback, ctx)
    }
  }

  trait ExperimentOutcome {

    import ExperimentOutcome.UndefinedFallback

    def getOrElse(fallback: String): String = {
      conduct(fallback)
    }

    def fold[U](fallback: => U)(f: PartialFunction[String, U]): Unit => U = {
      val group = conduct(UndefinedFallback)
      _ => {
        group match {
          case UndefinedFallback => fallback
          case other if f isDefinedAt other => f(other)
          case _ => fallback
        }
      }
    }

    protected def experimentClass[T <: SpecDefinition : ClassTag] = implicitly[ClassTag[T]].runtimeClass.asInstanceOf[Class[SpecDefinition]]

    protected def conduct(fallback: String): String
  }

  object ExperimentOutcome {
    private[petri] val UndefinedFallback = "0e0f734b-606d-4675-a81e-bc4a21710143"
  }

}



