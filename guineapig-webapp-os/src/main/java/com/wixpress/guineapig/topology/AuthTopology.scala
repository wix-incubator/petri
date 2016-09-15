package com.wixpress.guineapig.topology

import scala.beans.BeanProperty

case class AuthTopology(@BeanProperty applicationName: String,
                        @BeanProperty clientId: String,
                        @BeanProperty clientSecret: String,
                        @BeanProperty redirectUri: String)
