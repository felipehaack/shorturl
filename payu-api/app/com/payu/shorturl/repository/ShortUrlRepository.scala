package com.payu.shorturl.repository

import java.sql.Connection

import com.payu.shorturl.model.{Url, UrlId}
import com.payu.shorturl.persistence.Sql

class ShortUrlRepository extends Sql {

  implicit private val ShortUrlParser = parser { row =>
    Url(
      id = row.long("id"),
      url = row.string("url"),
      urlShortened = row.stringOption("url_shortened"),
      urlHashed = row.string("url_hashed"),
      created = row.localDateTime("created")
    )
  }


  def find(urlId: UrlId)(implicit conn: Connection): Option[Url] = {

    val query = sql"""
         SELECT * FROM urls WHERE id = ${urlId} AND deleted IS NULL
       """

    query.asSingleOption[Url]
  }

  def findByUrl(url: String)(implicit conn: Connection): Option[Url] = {

    val query = sql"""
         SELECT * FROM urls WHERE url = ${url} AND deleted IS NULL
       """

    query.asSingleOption[Url]
  }

  def findByUrlShortened(urlShortened: String)(implicit conn: Connection): Option[Url] = {

    val query = sql"""
         SELECT * FROM urls WHERE url_shortened = ${urlShortened} AND deleted IS NULL
       """

    query.asSingleOption[Url]
  }

  def findByUrlHashed(urlHashed: String)(implicit conn: Connection): Option[Url] = {

    val query = sql"""
         SELECT * FROM urls WHERE url_hashed = ${urlHashed} AND deleted IS NULL
       """

    query.asSingleOption[Url]
  }

  def create(input: Url.Create, urlHashed: String)(implicit conn: Connection): UrlId = {

    val query = sql"""
         INSERT INTO urls (url, url_hashed) VALUES (${input.url}, ${urlHashed})
       """

    query.executeInsertLong()
  }

  def update(urlId: UrlId, input: Url.Update)(implicit conn: Connection): Boolean = {

    val query = sql"""
         UPDATE urls
         SET url_shortened = ${input.urlShortened}, url_hashed = ${input.urlHashed}
         WHERE id = ${urlId}
       """

    query.executeSingleUpdate()
  }
}
