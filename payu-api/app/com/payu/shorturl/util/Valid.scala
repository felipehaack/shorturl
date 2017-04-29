package com.payu.shorturl.util

import java.net.URL

import com.wix.accord.ViolationBuilder._

import scala.util.Try

import com.wix.accord.NullSafeValidator

object Valid {

  object Url extends NullSafeValidator[String](
    v => Try(new URL(v)).isSuccess,
    _ -> "is not a Url"
  )

  object Metadata extends NullSafeValidator[Map[String, String]](
    v => v.size <= 20 && v.forall { case (k, v) => k.length <= 40 && v.length <= 512 },
    _ -> "invalid metadata"
  )

  object OnlyNumber extends NullSafeValidator[String](
    v => v.matches("[0-9]+"),
    _ -> "does not has only number"
  )

}