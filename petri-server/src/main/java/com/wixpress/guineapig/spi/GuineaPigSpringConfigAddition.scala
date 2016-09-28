package com.wixpress.guineapig.spi

import com.wixpress.guineapig.dto.SpecExposureIdViewDto
import com.wixpress.guineapig.entities.ui.UiSpecForScope
import com.wixpress.guineapig.entities.ui.UiSpecForScopeBuilder.anUiSpec
import com.wixpress.guineapig.topology.GuineapigDBTopology
import com.wixpress.petri.Main
import com.wixpress.petri.experiments.domain.ScopeDefinition
import com.wixpress.petri.petri.FullPetriClient
import org.springframework.context.annotation.{Bean, Configuration}

import scala.collection.JavaConversions._

@Configuration
class GuineaPigSpringConfigAddition {

  @Bean
  def petriClient: FullPetriClient = Main.rpcServer


  // SPI BEANS

  @Bean
  private[guineapig] def hardCodedSpecsProvider: HardCodedScopesProvider = new HardCodedScopesProvider {
    override def getHardCodedScopesList: java.util.List[ScopeDefinition] = List(new ScopeDefinition("publicUrl", false)).toList
    override def getHardCodedScopes: Map[String, List[UiSpecForScope]] =
      Map("publicUrl" -> List( anUiSpec().withScope("publicUrl").withForRegisteredUsers(false).build())) //TODO!!! must match above or consolidate...
  }

  @Bean
  private[guineapig] def specExposureIdRetriever: SpecExposureIdRetriever = new SpecExposureIdRetriever {
    override def getAll: java.util.List[SpecExposureIdViewDto] = seqAsJavaList(Seq())
  }

  @Bean
  private[guineapig] def languageResolver: SupportedLanguagesProvider = new SupportedLanguagesProvider() {
    def getSupportedLanguages: Set[String] = Set("en", "de", "es", "il")
  }

  @Bean
  private[guineapig] def globalGroupsManagementService: GlobalGroupsManagementService =
    new GlobalGroupsManagementService() {
      def allGlobalGroups: Seq[String] = Seq.empty
    }

  @Bean
  private[guineapig] def dataSourceTopology: GuineapigDBTopology = {
    val gpDBTopology: GuineapigDBTopology = new GuineapigDBTopology
    val ourServerDBConfig = Main.dbConfig()
    gpDBTopology.url = ourServerDBConfig.url
    gpDBTopology.username = ourServerDBConfig.username
    gpDBTopology.password = ourServerDBConfig.password
    gpDBTopology
  }
}
