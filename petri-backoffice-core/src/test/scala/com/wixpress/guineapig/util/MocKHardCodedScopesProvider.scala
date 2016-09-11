package com.wixpress.guineapig.util

import com.google.common.collect.ImmutableList
import com.wixpress.guineapig.entities.ui.{UiSpecForScope, UiTestGroup}
import com.wixpress.guineapig.spi.HardCodedScopesProvider
import com.wixpress.guineapig.util.MockHardCodedScopesProvider._
import com.wixpress.petri.experiments.domain.ScopeDefinition

class MockHardCodedScopesProvider extends HardCodedScopesProvider {

  def getHardCodedScopes: Map[String, List[UiSpecForScope]] = {
    Map(
      HARD_CODED_SPEC_FOR_REG -> List(new UiSpecForScope(HARD_CODED_SPEC_FOR_REG, ImmutableList.of[UiTestGroup], "hardCodedScopeForReg", 0L, 0L, true, "na")),
      HARD_CODED_SPEC_FOR_NON_REG -> List(new UiSpecForScope(HARD_CODED_SPEC_FOR_NON_REG, ImmutableList.of[UiTestGroup], "hardCodedSpecForNonReg", 0L, 0L, false, "na"))
    )
  }

  def getHardCodedScopesList: java.util.List[ScopeDefinition] = scala.collection.JavaConverters.seqAsJavaListConverter(Seq(
    new ScopeDefinition(HARD_CODED_SPEC_FOR_REG, true),
    new ScopeDefinition(HARD_CODED_SPEC_FOR_NON_REG, false)
  )).asJava
}

object MockHardCodedScopesProvider{

  var HARD_CODED_SPEC_FOR_REG: String = "hardCodedSpecForReg"
  var HARD_CODED_SPEC_FOR_NON_REG: String = "hardCodedSpecForNonReg"

}

