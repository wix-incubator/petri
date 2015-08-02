package com.wixpress.petri.laboratory

/**
 * Created by talyag on 6/23/15.
 */
trait PetriConductionContextRetriever {
  def read(explicitlyProvidedContext : ConductionContext): ConductionContext
}

class DefaultConductionContextRetriever extends PetriConductionContextRetriever {

  override def read(explicitlyProvidedContext: ConductionContext): ConductionContext =
    if (explicitlyProvidedContext != null)
      explicitlyProvidedContext
    else
      ConductionContextBuilder.newInstance
}

trait PetriConductionContextSetter {
  def setContext(conductionContext: ConductionContext)
}

