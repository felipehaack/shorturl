package com.payu.shorturl.util

import play.api.libs.json._

object JsonUtil extends JsonEnum with JsonEither

trait JsonEnum {

  final def enumValueReads[E <: Enumeration](enum: E): Reads[E#Value] = Reads {
    case JsString(s) =>
      try {
        JsSuccess(enum.withName(s))
      } catch {
        case _: NoSuchElementException =>
          JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not appear to contain the value: '$s'")
      }
    case _ => JsError("String value expected")
  }

  final def enumIdReads[E <: Enumeration](enum: E): Reads[E#Value] = Reads {
    case JsNumber(id) =>
      try {
        JsSuccess(enum(id.intValue()))
      } catch {
        case _: NoSuchElementException =>
          JsError(s"Enumeration expected of type: '${enum.getClass}', but it does not appear to contain the value: '$id'")
      }
    case _ => JsError("String value expected")
  }

  final def enumValueWrites[E <: Enumeration](): Writes[E#Value] = {
    Writes { v: E#Value => JsString(v.toString) }
  }

  final def enumIdWrites[E <: Enumeration](): Writes[E#Value] = {
    Writes { v: E#Value => JsNumber(v.id) }
  }

  final def enumValueFormat[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(enumValueReads(enum), enumValueWrites())
  }

  final def enumIdFormat[E <: Enumeration](enum: E): Format[E#Value] = {
    Format(enumIdReads(enum), enumIdWrites())
  }
}

trait JsonEither {

  final def eitherReads[A: Reads, B: Reads]: Reads[Either[A, B]] = {
    Reads { json =>
      json.validate[A] match {
        case JsSuccess(value, path) => JsSuccess(Left(value), path)
        case JsError(aError) =>
          json.validate[B] match {
            case JsSuccess(value, path) => JsSuccess(Right(value), path)
            case JsError(bError) => JsError(JsError.merge(aError, bError))
          }
      }
    }
  }

  final def eitherWrites[A: Writes, B: Writes]: Writes[Either[A, B]] = {
    Writes {
      case Left(a) => Json.toJson(a)
      case Right(b) => Json.toJson(b)
    }
  }

  final def eitherFormat[A: Format, B: Format]: Format[Either[A, B]] = {
    Format(eitherReads, eitherWrites)
  }
}

