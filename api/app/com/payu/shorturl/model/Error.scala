package com.payu.shorturl.model

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Error(
                  code: String,
                  message: String,
                  errors: Option[Set[Error]] = None
                )

object Error {
  implicit val format: Format[Error] = (
    (JsPath \ 'code).format[String]
      and (JsPath \ 'message).format[String]
      and (JsPath \ 'errors).lazyFormatNullable(Format(Reads.set[Error], Writes.set[Error]))
    ) (Error.apply, unlift(Error.unapply))

}

