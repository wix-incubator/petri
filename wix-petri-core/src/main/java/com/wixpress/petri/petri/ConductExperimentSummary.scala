package com.wixpress.petri.petri

import org.joda.time.DateTime

/**
 * Petri - (c) Wix LTD. http://www.wix.com
 */
case class ConductExperimentSummary(serverName: String, experimentId: Int, experimentValue: String, fiveMinuteCount:  Long,
                                    totalCount:  Long, lastUpdated: DateTime)
