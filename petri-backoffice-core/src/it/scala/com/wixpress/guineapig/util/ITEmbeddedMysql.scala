package com.wixpress.guineapig.util

import java.util.TimeZone
import javax.sql.DataSource

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
import com.wix.mysql.EmbeddedMysql
import com.wix.mysql.config.Charset._
import com.wix.mysql.config.MysqldConfig._
import com.wix.mysql.distribution.Version._

class ITEmbeddedMysql(port: Int = 3310) {
  val Config = aMysqldConfig(v5_6_latest)
    .withPort(port)
    .withCharset(UTF8)
    .withTimeZone(TimeZone.getTimeZone("UTC"))
    .build

  val Schema = "test_db"
  var instance: EmbeddedMysql = _

  def start(): Unit = {
    instance = EmbeddedMysql.anEmbeddedMysql(Config).addSchema(Schema).start
  }

  def stop(): Unit = instance.stop()

  def dataSource: DataSource = {
    val dataSource: MysqlDataSource = new MysqlDataSource
    dataSource.setUrl(s"jdbc:mysql://localhost:${Config.getPort}/$Schema")
    dataSource.setUser(Config.getUsername)
    dataSource.setPassword(Config.getPassword)
    dataSource.setConnectTimeout(2000)
    dataSource
  }
}
