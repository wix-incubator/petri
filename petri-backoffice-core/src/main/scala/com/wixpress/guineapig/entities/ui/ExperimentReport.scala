package com.wixpress.guineapig.entities.ui

import org.joda.time.DateTime

case class ExperimentReport(experimentId: Int,
                            fiveMinuteCount: Long,
                            totalCount: Long,
                            lastUpdated: DateTime,
                            reportsPerValue: Seq[PerValueReport])

case class PerValueReport(experimentValue: String,
                          fiveMinuteCount: Long,
                          totalCount: Long,
                          lastUpdated: DateTime,
                          reportsPerServer: Seq[PerServerReport])

case class PerServerReport(serverName: String,
                           fiveMinuteCount: Long,
                           totalCount: Long,
                           lastUpdated: DateTime)
