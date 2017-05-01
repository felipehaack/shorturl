package com.payu.shorturl.service

import javax.inject.{Inject, Singleton}

import com.payu.shorturl.exception.ShortUrlException
import com.payu.shorturl.model.Url
import com.payu.shorturl.persistence.DB
import com.payu.shorturl.repository.ShortUrlRepository
import com.payu.shorturl.util.{ShortUrl, Validator}

import scala.concurrent.Future
import scala.language.implicitConversions

@Singleton
class ShortUrlService @Inject()(
                                 db: DB,
                                 shortUrl: ShortUrl,
                                 shortUrlRepository: ShortUrlRepository
                               ) extends Service {

  private implicit def urlToList(url: Url): List[Url] = List(url)

  def create(input: Url.Create): Future[Url.Result] = {

    Validator.input(input)

    db.withTransaction { implicit conn =>

      val urlHashed = shortUrl.toMD5(input.url)

      shortUrlRepository.findByUrlHashed(urlHashed) match {
        case Some(url) => enrichUrl(url).head
        case None =>
          val urlId = shortUrlRepository.create(input, urlHashed)

          val urlShortened = shortUrl.encode(urlId)
          val urlUpdate = Url.Update(urlShortened, urlHashed)

          shortUrlRepository.update(urlId, urlUpdate) match {
            case true =>
              shortUrlRepository.find(urlId) match {
                case Some(url) => enrichUrl(url).head
                case None => ShortUrlException.notFound[Url]
              }
            case false => ShortUrlException.notFound[Url]
          }
      }
    }
  }

  def getByUrlShortened(input: String): Future[Option[Url]] = {

    db.withConnection { implicit conn =>

      shortUrlRepository.findByUrlShortened(input)
    }
  }

  private def enrichUrl(url: List[Url]): List[Url.Result] = {

    for {
      localUrl <- url
      urlShortened <- localUrl.urlShortened
    } yield Url.Result(
      urlShortened = urlShortened
    )
  }
}
