package com.wixpress.petri.laboratory

import com.fasterxml.jackson.databind.node.ObjectNode

/**
 * Created with IntelliJ IDEA.
 * User: daniels
 * Date: 3/31/14
 */
trait BIAdditions {
  def contributeToBi(contributor: BIContributor): Unit
}

object BIAdditions {

  val Empty = new BIAdditions {
    def contributeToBi(contributor: BIContributor): Unit = Unit
  }

  def merge(additions: BIAdditions, moreAdditions: BIAdditions) = new BIAdditions {
    override def contributeToBi(contributor: BIContributor): Unit = {
      additions.contributeToBi(contributor)
      moreAdditions.contributeToBi(contributor)
    }
  }
}

trait BIContributor {
  def put(name: String, value: String): this.type
  def put(name: String, value: Int): this.type
  def put(name: String, value: Boolean): this.type
  def put(name: String, value: Long): this.type
}

object BIContributor {

  def forJackson(node: ObjectNode) = new BIContributor {
    def put(name: String, value: Long): this.type = { node.put(name, value); this }
    def put(name: String, value: Boolean): this.type = { node.put(name, value); this }
    def put(name: String, value: Int): this.type = { node.put(name, value); this }
    def put(name: String, value: String): this.type = { node.put(name, value); this }
  }
}


