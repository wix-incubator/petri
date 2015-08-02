package com.wixpress.petri.laboratory

/**
 * Created by talyag on 7/9/15.
 */
class DummyBiContributor extends BIContributor {
  var values: Map[String, String] = Map()

  def put(name: String, value: String): this.type = {
    values += (name -> value)
     this
  }

  def put(name: String, value: Int): this.type = this

  def put(name: String, value: Boolean): this.type = this

  def put(name: String, value: Long): this.type = this
}