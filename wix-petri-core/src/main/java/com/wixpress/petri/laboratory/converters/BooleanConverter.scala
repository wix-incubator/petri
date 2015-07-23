package com.wixpress.petri.laboratory.converters


import com.wixpress.petri.laboratory.TestResultConverter


/** A [[com.wixpress.petri.laboratory.TestResultConverter]] that converts test results to [[scala.Boolean]] type;
  * issuing a `convert` on a "true" string yields the boolean `true`, while issuing the `convert` operation on a
  * "false" string yields the boolean `false`. The given string is treated as case insensitive, meaning, issuing
  * `covert("true")` is the same as issuing `convert("TrUe")` (the same applies for "false" string).
  * If the given string cannot be parsed to its boolean representation, i.e., neither "true" nor "false"
  * (case insensitive), the method throws an exception ([[java.lang.IllegalArgumentException]]).
  *
  * @see [[com.wixpress.petri.laboratory.TestResultConverter]]
  * @see [[com.wixpress.petri.laboratory.Laboratory.conductExperiment[T]: T]]
  * @see [[com.wixpress.petri.laboratory.converters.BooleanConverterTest]]
  *
  * @author <a href="mailto:ohadr@wix.com">Raz, Ohad</a>
  */
class BooleanConverter extends TestResultConverter[Boolean] {

  override def convert(value: String) : Boolean = {
    value.toBoolean
  }
}
