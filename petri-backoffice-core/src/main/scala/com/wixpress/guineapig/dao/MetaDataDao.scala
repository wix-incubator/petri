package com.wixpress.guineapig.dao

import java.{util => ju}

import com.fasterxml.jackson.databind.ObjectMapper
import com.wixpress.guineapig.dto.MetaData
import org.springframework.jdbc.core.JdbcTemplate

import scala.collection.JavaConversions._
import scala.reflect.ClassTag

trait MetaDataDao {
  def add[T <: MetaData](metaData: T)

  def delete[T <: MetaData](metaDataType: Class[T], metaDataId: String)

  def get[T <: MetaData](metaDataType: Class[T]): ju.List[T]
}

class MySqlMetaDataDao(jdbcTemplate: JdbcTemplate, objectMapper: ObjectMapper) extends MetaDataDao {

  override def add[T <: MetaData](metaData: T) {
    var existingMetaData: Set[T] = get(metaData.getClass).toSet
    existingMetaData = addMetaDataToSet(existingMetaData, metaData)

    editMetaData(existingMetaData, metaData.getClass)
  }

  def addMetaDataToSet[T <: MetaData](set: Set[T], id: T): Set[T] = set + id

  override def delete[T <: MetaData](metaDataType: Class[T], metaDataId: String) {
    val existingMetaData = deleteMetaDataFromSet(get(metaDataType).toSet, metaDataId)
    editMetaData(existingMetaData, metaDataType)
  }

  override def get[T <: MetaData](metaDataType: Class[T]): ju.List[T] = {
    val sql = getMetaDataQuery(metaDataType.getSimpleName)
    val result = jdbcTemplate.queryForList(sql, classOf[String])
    result.flatMap(asListOf[T](_)(ClassTag(metaDataType), objectMapper))
  }

  private def getMetaDataQuery(dataType: String) = s"SELECT data_value FROM meta_data WHERE data_type = '$dataType'"

  def asListOf[T](s: String)(implicit ct: ClassTag[T], mapper: ObjectMapper): ju.List[T] =
    mapper.readValue(s, mapper.getTypeFactory.constructCollectionType(classOf[ju.List[T]], ct.runtimeClass))

  def editMetaData[T <: MetaData](existingMetaData: Set[T], metaDataType: Class[_ <: T]): Unit = {
    val dataValue = objectMapper.writeValueAsString(existingMetaData)
    val sql = addMetaDataQuery(metaDataType.getSimpleName, dataValue)
    jdbcTemplate.update(sql, metaDataType.getSimpleName, dataValue, dataValue)
  }

  private def addMetaDataQuery(dataType: String, metaData: String) = {
    "INSERT INTO meta_data (data_type, data_value)  VALUES (?, ?) ON DUPLICATE KEY UPDATE data_value = ?"
  }

  def deleteMetaDataFromSet[T <: MetaData](set: Set[T], id: String): Set[T] = {
    if (!set.exists(_.matchesId(id))) throw new MetaDataNotFound(id)
    set.filterNot(_.matchesId(id))
  }

  class MetaDataNotFound(id: String) extends RuntimeException(s"Failed Deleting Meta Data with id $id: Does not exist")

}
