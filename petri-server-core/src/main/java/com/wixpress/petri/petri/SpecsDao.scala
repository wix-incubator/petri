package com.wixpress.petri.petri

import com.wixpress.petri.experiments.domain.ExperimentSpec
import org.joda.time.DateTime

/**
 * @author dmitryk
 * @since 21-Sep-2015
 */
trait SpecsDao {

  def add(spec: ExperimentSpec): ExperimentSpec

  def update(spec: ExperimentSpec, currentDateTime: DateTime): Unit

  def delete(key: String): Unit

  def fetch(): Seq[ExperimentSpec]

}
