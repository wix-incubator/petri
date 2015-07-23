package com.wixpress.petri.experiments.domain

import org.joda.time.DateTime
import scala.beans.BooleanBeanProperty
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */


@JsonDeserialize(builder = classOf[ExperimentSnapshotBuilder])
case class ExperimentSnapshot(
                               key: String,
                               @BooleanBeanProperty fromSpec: Boolean, //not needed for conductableExperiment
                               creationDate: DateTime, //not needed for conductableExperiment
                               description: String, //not needed for conductableExperiment
                               startDate: DateTime,
                               endDate: DateTime,
                               groups: java.util.List[TestGroup],
                               scope: String,
                               @BooleanBeanProperty paused: Boolean,
                               name: String, //not needed for conductableExperiment
                               creator: String, //not needed for conductableExperiment
                               @BooleanBeanProperty featureToggle: Boolean,
                               originalId: Int,
                               linkedId: Int,
                               @Deprecated @BooleanBeanProperty persistent: Boolean,
                               filters: java.util.List[Filter],
                               @BooleanBeanProperty onlyForLoggedInUsers: Boolean,
                               comment: String,
                               updater: String, //not needed for conductableExperiment
                               conductLimit: Int
                               ) {
  //not needed for conductableExperiment


  @JsonIgnore
  def isValid: Boolean = new FiltersValidator().checkValidity(filters)

}
