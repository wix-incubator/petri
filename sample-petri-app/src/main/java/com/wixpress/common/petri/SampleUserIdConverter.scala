package com.wixpress.common.petri

import java.util.{Base64, UUID}

import com.wixpress.petri.laboratory.Converter

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
  def encode(value: UUID): String = new String(Base64.getEncoder.encode(value.toString.getBytes))
  def decode(value: String): UUID  = UUID.fromString(new String(Base64.getDecoder.decode(value.getBytes)))
}
