package com.wixpress.guineapig.spring

import java.util
import com.wixpress.guineapig.dao.MetaDataDao
import com.wixpress.guineapig.dto.SpecExposureIdViewDto
import com.wixpress.guineapig.entities.ui.UiSpecForScope
import com.wixpress.guineapig.services._
import com.wixpress.guineapig.spi.{GlobalGroupsManagementService, HardCodedScopesProvider, SpecExposureIdRetriever, SupportedLanguagesProvider}
import com.wixpress.petri.experiments.domain.ScopeDefinition
import com.wixpress.petri.petri.{FullPetriClient, RAMPetriClient}
import org.springframework.context.annotation.Bean
import scala.collection.JavaConverters._
import scala.collection.Seq
import scala.collection.immutable.Set

class SpringConfig {

  @Bean
  def fullPetriClient: FullPetriClient = new RAMPetriClient

  @Bean
  def hardCodedSpecsProvider: HardCodedScopesProvider = new HardCodedScopesProvider {
      override def getHardCodedScopes: Map[String, List[UiSpecForScope]] = Map.empty[String, List[UiSpecForScope]]

      override def getHardCodedScopesList: util.List[ScopeDefinition] = {
        Seq(new ScopeDefinition("publicUrl", false)).asJava
      }
  }

  @Bean
  def metaDataService(metaDataDao: MetaDataDao,
                      fullPetriClient: FullPetriClient,
                      hardCodedSpecsProvider: HardCodedScopesProvider,
                      specExposureIdRetriever: SpecExposureIdRetriever,
                      languageResolver: SupportedLanguagesProvider,
                      globalGroupsManagementService: GlobalGroupsManagementService): MetaDataService = {
    new MetaDataService(metaDataDao, fullPetriClient, hardCodedSpecsProvider, specExposureIdRetriever, languageResolver, globalGroupsManagementService)
  }

  @Bean
  def specExposureIdRetriever: SpecExposureIdRetriever = new SpecExposureIdRetriever() {
    override def getAll: util.List[SpecExposureIdViewDto] = Nil.asJava
  }

  @Bean
  def languageResolver: SupportedLanguagesProvider = new SupportedLanguagesProvider() {
    override def getSupportedLanguages: Set[String] = Set("en", "de", "es")
  }

  @Bean
  def globalGroupsManagementService: GlobalGroupsManagementService = new GlobalGroupsManagementService() {
    override def allGlobalGroups: Seq[String] = Seq.empty[String]
  }

}