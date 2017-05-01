package api

import javax.inject.{Inject, Singleton}

import com.payu.shorturl.model.Url
import com.payu.shorturl.service.ShortUrlService

@Singleton
class ShortUrlApi @Inject()(
                             shortUrlService: ShortUrlService
                           ) extends Api {

  def create() = Action.async(json[Url.Create]) { implicit request =>

    val input = request.body

    val result = shortUrlService.create(input)

    Ok.asJson(result)
  }

  def get(urlShortened: String) = Action.async { implicit request =>

    shortUrlService.getByUrlShortened(urlShortened) map {
      case Some(localUrl) => TemporaryRedirect(localUrl.url)
      case None => NotFound
    }
  }
}
