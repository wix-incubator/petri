package com.wixpress.petri.petri

import org.joda.time.DateTime

/**
 * User: Dalias
 * Date: 12/15/14
 * Time: 1:51 PM
 */
case class ConductExperimentSummary(serverName: String, experimentId: Int, experimentValue: String, fiveMinuteCount:  Long,
                                    totalCount:  Long, lastUpdated: DateTime)
