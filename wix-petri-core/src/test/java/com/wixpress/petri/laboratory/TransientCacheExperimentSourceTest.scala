package com.wixpress.petri.laboratory

import java.util

import com.wix.hoopoe.koboshi.cache.TimestampedData
import com.wix.hoopoe.koboshi.cache.transience.AtomicReferenceCache
import com.wixpress.petri.ExperimentsAndState
import com.wixpress.petri.experiments.domain.{ConductibleExperiments, Experiment, FakeClock}
import org.joda.time.{Instant, Minutes}
import org.specs2.matcher.Matcher
import org.specs2.mutable.SpecificationWithJUnit
import org.specs2.specification.Scope

class TransientCacheExperimentSourceTest extends SpecificationWithJUnit {

  trait Context extends Scope {
    val underlyingCache = new AtomicReferenceCache[ConductibleExperiments]
    val koboshiInterval = 5
    val remoteFetchingLatencyDuration = 1
    val aPointInTime = new Instant(5)
    val timeToStale = Minutes.minutes(koboshiInterval).plus(remoteFetchingLatencyDuration).toStandardDuration
  }


  "TransientCacheExperimentSource" should {
    "reflect data is fresh when it was updated less than 5 minutes, the koboshi fetch interval, ago" in new Context {
      val clock = new FakeClock(aPointInTime.plus(timeToStale).minus(1).toDateTime)
      val source = new TransientCacheExperimentSource(underlyingCache, clock)
      underlyingCache.write(dataWithTimestamp(aPointInTime))

      source.read() must beFresh()

    }
    "reflect data is stale when it was updated more than 5 minutes, the fetch interval, ago" in new Context {
      val clock = new FakeClock(aPointInTime.plus(timeToStale).plus(1).toDateTime)
      val source = new TransientCacheExperimentSource(underlyingCache, clock)
      underlyingCache.write(dataWithTimestamp(aPointInTime))

      source.read() must beStale()
    }

  }

  def dataWithTimestamp(updateTime: Instant): TimestampedData[ConductibleExperiments] =
    new TimestampedData[ConductibleExperiments](ConductibleExperiments(new util.ArrayList[Experiment]()), updateTime)
  def beFresh(): Matcher[ExperimentsAndState] = beStaleOrFresh(staleness = beFalse)
  def beStale(): Matcher[ExperimentsAndState] = beStaleOrFresh(staleness = beTrue)
  def beStaleOrFresh(staleness : Matcher[Boolean]): Matcher[ExperimentsAndState] = staleness ^^ { e: ExperimentsAndState => e.stale aka "staleness indicator"}

}
