package com.wixpress.guineapig.services

import java.util
import java.util.Locale

import com.wixpress.guineapig.dao.MetaDataDao
import com.wixpress.guineapig.dto.{SpecExposureIdViewDto, UserAgentRegex}
import com.wixpress.guineapig.entities.ui._
import com.wixpress.guineapig.spi.{GlobalGroupsManagementService, HardCodedScopesProvider, SpecExposureIdRetriever, SupportedLanguagesProvider}
import com.wixpress.petri.experiments.domain.{ExperimentSpec, ScopeDefinition}
import com.wixpress.petri.petri.FullPetriClient

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.util.Try

class MetaDataService(metaDataDao: MetaDataDao,
                      fullPetriClient: FullPetriClient,
                      hardCodedScopesProvider: HardCodedScopesProvider,
                      specExposureIdRetriever: SpecExposureIdRetriever,
                      supportedLanguageResolver: SupportedLanguagesProvider,
                      globalGroupsManagementService: GlobalGroupsManagementService)
  extends EditableMetaDataService {

  def getGeoList: util.List[FilterOption] = MetaDataService.COUNTRIES_GROUPS union Locale.getISOCountries.map { country => new FilterOption(country, new Locale("en", country).getDisplayCountry) }

  def getUserAgentRegexList: util.List[FilterOption] = metaDataDao.get(classOf[UserAgentRegex]).map(regex => new FilterOption(regex.regex, regex.description))

  def getUserGroupsList: util.List[FilterOption] = Try(globalGroupsManagementService.allGlobalGroups).getOrElse(Seq()).toList.map(group => new FilterOption(group, group))

  def getLangList: util.List[FilterOption] = supportedLanguageResolver.getSupportedLanguages.map(language => new FilterOption(language, new Locale(language).getDisplayLanguage)).toList

  def createScopeToSpecMap: util.Map[String, util.List[UiSpecForScope]] = {
    (hardCodedScopesProvider.getHardCodedScopes ++ collectSpecsFromPetri).toMap.asInstanceOf[Map[String, util.List[UiSpecForScope]]]
  }

  private def collectSpecsFromPetri: List[(String, util.List[UiSpecForScope])] = {
    val allExperiments = fullPetriClient.fetchAllExperimentsGroupedByOriginalId
    createScopeToSpecMap(fullPetriClient.fetchSpecs).mapValues(seqAsJavaList(_)).toList
  }

  def createScopeToSpecMap(allSpecs: util.List[ExperimentSpec]): Map[String, List[UiSpecForScope]] = {
    val specExposureIdMap: Map[String, SpecExposureIdViewDto] = getSpecExposureMapFromDB

    val scopesToSpecs: List[(ScopeDefinition, ExperimentSpec)] = allSpecs.toList
      .flatMap(
        spec => spec
          .getScopes
          .groupBy(scope => scope.isOnlyForLoggedInUsers)
          .map { case (isForRegistered: Boolean, scopes: mutable.Buffer[ScopeDefinition]) =>
            new ScopeDefinition(scopes.map(_.getName).mkString(","), isForRegistered)
          }
          .map(scope => scope -> spec)
      )

    val scopeNameToSpecs = scopesToSpecs.groupBy(_._1.getName)

    scopeNameToSpecs.mapValues {
      myList => myList.map {
        case (scope: ScopeDefinition, spec: ExperimentSpec) =>
          val exposureId = specExposureIdMap.get(spec.getKey).flatMap(_.exposureId)
          UiSpecForScopeBuilder.anUiSpec
            .withGroups(createTestGroupList(spec.getTestGroups))
            .withKey(spec.getKey)
            .withScope(scope.getName)
            .withStartDate(-1)
            .withEndDate(-1)
            .withForRegisteredUsers(scope.isOnlyForLoggedInUsers)
            .withExposureId(exposureId.orNull)
            .build
      }
    }

  }

  def createTestGroupList(testGroups: util.List[String]): util.List[UiTestGroup] = testGroups.map(new UiTestGroup(0, _, 0))

  def getSpecExposureMapFromDB: Map[String, SpecExposureIdViewDto] = specExposureIdRetriever.getAll.map(exposure => exposure.spec -> exposure).toMap

  def addUserAgentRegex(userAgentRegex: UserAgentRegex) {
    metaDataDao.add(userAgentRegex)
  }

  def deleteUserAgentRegex(regex: String) {
    metaDataDao.delete(classOf[UserAgentRegex], regex)
  }

}

object MetaDataService {
  val COUNTRIES_GROUPS: util.List[FilterOption] = List(
    FilterOption("GB,TR,NL,KR,PE", "Editor (United Kingdom, Turkey, Netherlands, South Korea & Peru )"),
    FilterOption("ES,RU,AU,PT,CL", "Components (Spain, Russia, Australia, Portugal & Chile)"),
    FilterOption("FR,MX,DE,PH,ZA", "Consumer Experience AKA Web (France, Mexico, Germany, Philippines & South Africa)"),
    FilterOption("CA,JP,IT,AR,PL", "Lists (Canada, Japan, Italy, Argentina & Poland)"),
    FilterOption("BR,CO,IN,CH,BE", "No Name (Brazil, Colombia, India, Switzerland & Belgium)"))

}


