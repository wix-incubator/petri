package com.wixpress.guineapig.spi

trait SupportedLanguagesProvider {
    def getSupportedLanguages: Set[String]

}
