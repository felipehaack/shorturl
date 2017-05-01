package com.payu.shorturl.model

import java.time.LocalDateTime

import com.payu.shorturl.util.Valid
import com.wix.accord.dsl._

@json case class UrlId(value: Long) extends TypedId

case class Url(
                id: UrlId,
                url: String,
                urlShortened: Option[String],
                urlHashed: String,
                created: LocalDateTime
              )

object Url {

  @jsonstrict case class Create(
                                 url: String
                               )

  object Create {

    implicit val Validator = validator[Create] { v =>
      v.url has size.between(5, 1024)
      v.url is Valid.Url
    }
  }

  case class Update(
                     urlShortened: String,
                     urlHashed: String
                   )

  @jsonstrict case class Result(
                                 urlShortened: String
                               )

}