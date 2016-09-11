package com.wixpress.guineapig.spi

import com.wixpress.guineapig.entities.ui.UiSpecForScope
import com.wixpress.petri.experiments.domain.ScopeDefinition

//TODO - improve this api, this sucks (consolidate to 1?)
trait HardCodedScopesProvider{
  def getHardCodedScopes: Map[String, List[UiSpecForScope]]
  def getHardCodedScopesList: java.util.List[ScopeDefinition]
}
