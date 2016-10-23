package com.wixpress.petri.laboratory.http

import com.wix.hoopoe.koboshi.cache.ReadOnlyTimestampedLocalCache
import com.wix.hoopoe.koboshi.remote.RemoteDataSource
import com.wix.hoopoe.koboshi.servlet.ResilientCacheRegistryEndpoint
import com.wixpress.petri.experiments.domain.ConductibleExperiments
import com.wixpress.petri.petri.PetriClient
import javax.servlet.ServletContext
import java.io.File
import java.nio.file.Paths

import com.wix.hoopoe.koboshi.cache.defaults.ResilientCaches

object KoboshiServletSetup {
  def setupKoboshiCache(context: ServletContext, petriClient: PetriClient): ReadOnlyTimestampedLocalCache[ConductibleExperiments] = {
    val (caches, registry) = ResilientCaches.resilientCachesAndRegistry(koboshiCacheFolder())
    context.setAttribute(ResilientCacheRegistryEndpoint.RegistryKey, registry)
    caches.aResilientCacheBuilder[ConductibleExperiments](new PetriClientRemoteDataSource(petriClient)).withTimestampedData.build()
  }

  private def koboshiCacheFolder(): File = {
    val koboshiCacheFolder: File = Paths.get(System.getProperty("user.home"), "koboshi").toFile
    koboshiCacheFolder.mkdirs
    koboshiCacheFolder
  }

  private class PetriClientRemoteDataSource(val petriClient: PetriClient) extends RemoteDataSource[ConductibleExperiments] {
    def fetch(): ConductibleExperiments = ConductibleExperiments(petriClient.fetchActiveExperiments)
  }

}