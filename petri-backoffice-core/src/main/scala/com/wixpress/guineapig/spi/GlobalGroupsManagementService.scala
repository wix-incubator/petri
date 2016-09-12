package com.wixpress.guineapig.spi

trait GlobalGroupsManagementService {
  def allGlobalGroups: Seq[String]
}
