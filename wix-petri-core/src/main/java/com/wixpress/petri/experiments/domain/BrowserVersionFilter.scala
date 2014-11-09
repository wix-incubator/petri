package com.wixpress.petri.experiments.domain

import com.wixpress.petri.laboratory.BrowserVersion
import scala.collection.JavaConversions._

/**
 * User: Dalias
 * Date: 9/7/14
 * Time: 5:01 PM
 */
case class BrowserVersionFilter(browserVersions: java.util.List[BrowserVersion], exclude: Boolean) extends Filter {


  override def isEligible(filterEligibility: EligibilityCriteria): Boolean = {

    browserVersions.find(_.browserName == filterEligibility.getBrowserVersion.browserName) match {
      case None if exclude => true
      case None => false
      case Some(bv) => filterEligibility.getBrowserVersion.version >= bv.version
    }
  }
}
