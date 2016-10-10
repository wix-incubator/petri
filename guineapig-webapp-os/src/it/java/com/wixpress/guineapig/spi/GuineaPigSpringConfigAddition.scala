package com.wixpress.guineapig.spi

import java.util

import com.wixpress.guineapig.drivers.GlobalEnv
import com.wixpress.guineapig.dto.SpecExposureIdViewDto
import com.wixpress.guineapig.entities.ui.UiSpecForScope
import com.wixpress.guineapig.topology.GuineapigDBTopology
import com.wixpress.petri.experiments.domain.ScopeDefinition
import com.wixpress.petri.petri.{FullPetriClient}
import org.springframework.context.annotation.{Bean, Configuration}

import scala.collection.JavaConversions._

@Configuration
class GuineaPigSpringConfigAddition {

  @Bean
  def petriClientMock: FullPetriClient = GlobalEnv.fullPetriClient

  // SPI BEANS

  @Bean
  private[guineapig] def hardCodedSpecsProvider: HardCodedScopesProvider = new HardCodedScopesProvider {
    override def getHardCodedScopesList: java.util.List[ScopeDefinition] = List(new ScopeDefinition("publicUrl", false)).toList
    override def getHardCodedScopes: Map[String, List[UiSpecForScope]] = Map() //TODO!!! must match above or consolidate...
  }

  @Bean
  private[guineapig] def specExposureIdRetriever: SpecExposureIdRetriever = new SpecExposureIdRetriever {
    override def getAll: util.List[SpecExposureIdViewDto] = seqAsJavaList(Seq())
  }

  @Bean
  private[guineapig] def languageResolver: SupportedLanguagesProvider = new SupportedLanguagesProvider() {
    def getSupportedLanguages: Set[String] = Set("en", "de", "es")
  }

  @Bean
  private[guineapig] def globalGroupsManagementService: GlobalGroupsManagementService =
    new GlobalGroupsManagementService() {
      def allGlobalGroups: Seq[String] = Seq.empty
    }

  @Bean
  private[guineapig] def dataSourceTopology: GuineapigDBTopology = {
    new GuineapigDBTopology
  }
}
