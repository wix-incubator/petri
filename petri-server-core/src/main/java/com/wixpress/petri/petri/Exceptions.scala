package com.wixpress.petri.petri

object Exceptions {
  class ExperimentNotFoundException(msg: String) extends RuntimeException(msg) {
  }
}
