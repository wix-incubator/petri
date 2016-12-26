package com.wixpress.petri.laboratory

import java.util.UUID

/**
 * Created by litalt on 25/12/16.
 */
class SampleUserIdConverter extends Converter[UUID] {
  def convert(value: String): UUID = {
    if (value == null) null
    else SampleUserIdConverter.decode(value)
  }
}

object SampleUserIdConverter {
  def encode(value: UUID): String = "lala#" + value.toString
  def decode(value: String): UUID  = UUID.fromString(value.split("#")(1))
}
