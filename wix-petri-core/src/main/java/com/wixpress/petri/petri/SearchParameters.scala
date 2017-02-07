package com.wixpress.petri.petri

case class SearchParameters(query: String = "", limit: Int = 50, offset: Int = 0, status: String = "open")