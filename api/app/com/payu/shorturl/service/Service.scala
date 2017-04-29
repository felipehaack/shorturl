package com.payu.shorturl.service

import com.payu.shorturl.exception.ShortUrlException
import com.payu.shorturl.util.Logging

import scala.reflect.ClassTag

trait Service extends Logging {

  implicit class RichOption[T: ClassTag](value: Option[T]) {
    def getOrNotFound = {
      value.getOrElse(ShortUrlException.notFound[T])
    }
  }

}