package com.wixpress.petri.laboratory

import java.util.UUID

import com.wixpress.petri.experiments.domain.AdditionalEligibilityCriteria
import com.wixpress.petri.laboratory.EligibilityCriteriaTypes.{CountryCriterion, LanguageCriterion}
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

/**
 * Created by talyag on 7/10/15.
 */
class ConductionContextTest extends SpecificationWithJUnit {

  class Context extends Scope {}

  "conduction context" should {

    "merge BI additions" in new Context {
      val contextWithAdditions: ConductionContextBuilder = ConductionContextBuilder.newInstance.withBIAdditions(new BIAdditions {
        override def contributeToBi(contributor: BIContributor): Unit = {
          contributor.put("key", "originalValue")
          contributor.put("someKey", "someValue")
        }
      })
      val contextWithMoreAdditions: ConductionContextBuilder = ConductionContextBuilder.newInstance.withBIAdditions(new BIAdditions {
        override def contributeToBi(contributor: BIContributor): Unit =
          contributor.put("key", "overridenValue")
      })

      val biContributor = new DummyBiContributor
      val mergedContext: ConductionContextBuilder = ConductionContext.mergeSecondWins(contextWithAdditions, contextWithMoreAdditions)
      mergedContext.biAdditions.contributeToBi(biContributor)

      biContributor.values must havePairs("someKey" -> "someValue", "key" -> "overridenValue")
    }

    "merge additional criteria" in new Context {
      val contextWithCriteria: ConductionContextBuilder = ConductionContextBuilder.newInstance
        .withCriterionOverride(new LanguageCriterion("originalLanguage"))
        .withCriterionOverride(new CountryCriterion("someCountry"))

      val contextWithMoreCriteria: ConductionContextBuilder = ConductionContextBuilder.newInstance
        .withCriterionOverride(new LanguageCriterion("overridenLanguage"))

      val mergedContext: AdditionalEligibilityCriteria = ConductionContext.mergeSecondWins(contextWithCriteria, contextWithMoreCriteria).additionalEligibilityCriteria
      mergedContext.getCriterion(classOf[CountryCriterion]).getValue must be_===("someCountry")
      mergedContext.getCriterion(classOf[LanguageCriterion]).getValue must be_===("overridenLanguage")
    }

    "prefer explicit strategy" in new Context{
      val contextWithStrategy = ConductionContextBuilder.newInstance
      .withConductionStrategy(new RegisteredUserInfoType(UUID.randomUUID()))

      private val anonConductionStrategy: AnonymousUserInfoType = new AnonymousUserInfoType()
      val contextWithOtherStrategy = ConductionContextBuilder.newInstance
        .withConductionStrategy(anonConductionStrategy)

      val mergedContext = ConductionContext.mergeSecondWins(contextWithStrategy, contextWithOtherStrategy)

      mergedContext.customConductionStrategy must beSome(anonConductionStrategy)
    }

  }

}
