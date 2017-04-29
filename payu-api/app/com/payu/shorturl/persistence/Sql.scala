package com.payu.shorturl.persistence

import java.sql.{Connection, Date, PreparedStatement, Time, Timestamp}
import java.time.{LocalDate, LocalDateTime, LocalTime}
import java.util.UUID

import com.lucidchart.relate.{Sql => rSql, _}
import com.payu.shorturl.model.TypedId
import org.postgresql.util.PGobject
import play.api.libs.json._

import scala.collection.JavaConverters._
import scala.language.implicitConversions

trait Sql extends RelateMapper with RelateQuery

sealed trait RelateQuery {
  protected val emptySql = "".toSql
  protected val commaSql = ",".toSql
  protected val andSql = " AND ".toSql
  protected val orSql = " OR ".toSql
  protected val whereSql = " WHERE ".toSql

  implicit class SqlString(string: String) {
    def toSql = InterpolatedQuery.fromParts(Seq(string), Nil)
  }

  implicit class SqlInterpolation(stringContext: StringContext) {
    def sql(args: Parameter*) = InterpolatedQuery.fromParts(stringContext.parts, args)
  }

  protected def sqlWhere(parts: Option[InterpolatedQuery]*): InterpolatedQuery = {
    val wheres = parts.flatten
    if (wheres.isEmpty) emptySql
    else whereSql + wheres.reduce(_ + andSql + _)
  }

  implicit class RickSql(sql: rSql) {

    def executeSingleUpdate()(implicit connection: Connection): Boolean = {
      sql.executeUpdate() == 1
    }

    def executeInsertUUID()(implicit connection: Connection): UUID = {
      sql.executeInsertSingle(_.resultSet.getObject(1).asInstanceOf[UUID])
    }
  }

}

sealed trait RelateMapper {

  protected final val parser = RowParser

  implicit class RickSqlResult(result: SqlRow) {

    def UUID(column: String): UUID = {
      result.strictObject(column) match {
        case v: UUID => v
        case value => error(column, value)
      }
    }

    def localTime(column: String): LocalTime = {
      result.resultSet.getTime(column).toLocalTime
    }

    def localDate(column: String): LocalDate = {
      localDateTime(column).toLocalDate
    }

    def localDateOption(column: String): Option[LocalDate] = {
      localDateTimeOption(column).map(_.toLocalDate)
    }

    def localDateTime(column: String): LocalDateTime = {
      result.resultSet.getTimestamp(column).toLocalDateTime
    }

    def localDateTimeOption(column: String): Option[LocalDateTime] = {
      Option(result.resultSet.getTimestamp(column)).map(_.toLocalDateTime)
    }

    def enumValue(enum: Enumeration, column: String) = {
      enum.withName(result.string(column))
    }

    def enumValueOption(enum: Enumeration, column: String) = {
      result.stringOption(column).map(v => enum.withName(v))
    }

    def enumValues(enum: Enumeration, column: String) = {
      seqString(column).map(v => enum.withName(v)).toSet
    }

    def json(column: String): JsValue = {
      result.strictObject(column) match {
        case v: org.postgresql.util.PGobject if v.getType == "json" || v.getType == "jsonb" => Json.parse(v.getValue)
        case value => error(column, value)
      }
    }

    def jsonAs[T: Reads](column: String): T = json(column).as[T]

    def seqString(column: String): Seq[String] = {
      seq(column).map(_.asInstanceOf[String])
    }

    private def seq(column: String): Seq[_] = {
      result.strictArray(column) match {
        case v: java.sql.Array => v.getArray.asInstanceOf[Array[_]].toList
        case value => error(column, value)
      }
    }

    def map(column: String): Map[String, String] = {
      result.strictObject(column) match {
        case v: org.postgresql.util.PGobject if v.getType == "json" || v.getType == "jsonb" =>
          val fields = Json.parse(v.getValue).as[JsObject].fields
          fields.map {
            case (key, n: JsString) => key -> n.as[String]
            case (key, n) => key -> n.toString
          }.toMap
        case v: java.util.HashMap[_, _] =>
          v.asInstanceOf[java.util.HashMap[String, String]].asScala.toMap
        case v: java.sql.Array =>
          v.getArray.asInstanceOf[Array[Array[String]]].map(e => e(0) -> e(1)).toMap
        case value =>
          throw new IllegalArgumentException(s"Cannot convert $value: ${value.asInstanceOf[AnyRef].getClass} to Map[String, String] for column ${column}")
      }
    }

    private def error(column: String, value: Any) = {
      throw new IllegalArgumentException(s"Cannot convert $value: ${value.getClass} to UUID for column ${column}")
    }
  }

  class LocalDateTimeParameter(value: LocalDateTime) extends SingleParameter {
    protected def set(statement: PreparedStatement, i: Int) = statement.setTimestamp(i, Timestamp.valueOf(value))
  }

  class LocalTimeParameter(value: LocalTime) extends SingleParameter {
    protected def set(statement: PreparedStatement, i: Int) = statement.setTime(i, Time.valueOf(value))
  }

  class LocalDateParameter(value: LocalDate) extends SingleParameter {
    protected def set(statement: PreparedStatement, i: Int) = statement.setDate(i, Date.valueOf(value))
  }

  object PGObjectParameter {
    def apply(typ: String, value: String) = {
      val obj = new PGobject()
      obj.setType(typ)
      obj.setValue(value)
      obj
    }
  }

  class MapParameter(value: Map[String, String]) extends SingleParameter {
    override protected def set(statement: PreparedStatement, i: Int): Unit = {
      val js = Json.toJson(value).toString()
      statement.setObject(i, PGObjectParameter("jsonb", js))
    }
  }

  class UUIDParameter(value: UUID) extends SingleParameter {
    override protected def set(statement: PreparedStatement, i: Int): Unit = {
      statement.setObject(i, PGObjectParameter("uuid", value.toString))
    }
  }

  class JsValueParameter(value: JsValue) extends SingleParameter {
    override protected def set(statement: PreparedStatement, i: Int): Unit = {
      statement.setObject(i, PGObjectParameter("jsonb", value.toString))
    }
  }

  implicit def fromTypedIdOption[ID <: TypedId](value: Option[ID]): SingleParameter = {
    value.map(id => Parameter.fromLong(id.value)).getOrElse(NullBigIntParameter)
  }

  implicit def fromTypedId[ID <: TypedId](value: ID): SingleParameter = value.value

  implicit def fromLocalDateTime(value: LocalDateTime): SingleParameter = new LocalDateTimeParameter(value)

  implicit def fromLocalDateTimeOption(value: Option[LocalDateTime]): SingleParameter = value.map(fromLocalDateTime).getOrElse(NullDateParameter)

  implicit def fromLocalDate(value: LocalDate): SingleParameter = new LocalDateParameter(value)

  implicit def fromLocalDateOption(value: Option[LocalDate]): SingleParameter = value.map(fromLocalDate).getOrElse(NullDateParameter)

  implicit def fromLocalTime(value: LocalTime): SingleParameter = new LocalTimeParameter(value)

  implicit def fromLocalTimeOption(value: Option[LocalTime]): SingleParameter = value.map(fromLocalTime).getOrElse(NullDateParameter)

  implicit def fromUUID(value: UUID): SingleParameter = new UUIDParameter(value)

  implicit def fromJsValue(value: JsValue): SingleParameter = new JsValueParameter(value)

  implicit def fromMap(value: Map[String, String]): SingleParameter = new MapParameter(value)

  implicit def fromMapOption(value: Option[Map[String, String]]): SingleParameter = new MapParameter(value.getOrElse(Map.empty))

  implicit def fromEnum[E <: Enumeration](value: E#Value): SingleParameter = value.toString
}


