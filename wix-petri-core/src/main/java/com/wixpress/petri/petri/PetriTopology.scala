package com.wixpress.petri.petri

import java.{lang=>jl}

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
trait PetriTopology {
  def getPetriUrl: String

  //TODO Need to be extracted the the common configuration  once it's written
  def getReportsScheduleTimeInMillis: jl.Long = 300000l

  def isWriteStateToServer = true
}