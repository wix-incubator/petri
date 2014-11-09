/*      __ __ _____  __                                              *\
**     / // // /_/ |/ /          Wix                                 **
**    / // // / /|   /           (c) 2006-2013, Wix LTD.             **
**   / // // / //   |            http://www.wix.com/                 **
**   \__/|__/_//_/| |                                                **
\*                |/                                                 */
package com.wixpress.petri.laboratory.converters


import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope


/** The Unit Test got the [[com.wixpress.petri.laboratory.converters.BooleanConverter]] class.
  *
  * @author <a href="mailto:ohadr@wix.com">Raz, Ohad</a>
  */
class BooleanConverterTest extends SpecificationWithJUnit {

  trait Ctx extends Scope {
    val converterUnderTest = new BooleanConverter()
  }


  "converting a string" should {
    "yeild the its boolean value" in new Ctx {
      converterUnderTest.convert("true") must be_==(true)
      converterUnderTest.convert("false") must be_==(false)
    }
  }

  "conversion of the string" should {
    "be case insensitive" in new Ctx {
      converterUnderTest.convert("TrUe") must be_==(true)
      converterUnderTest.convert("FAlSe") must be_==(false)
    }
  }

  "conversion of a string that does not represent a boolean value" should {
    "fail with exception" in new Ctx {
      converterUnderTest.convert("kuki buki") must throwAn[IllegalArgumentException]
    }
  }
}
