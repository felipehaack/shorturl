package api

import com.payu.shorturl.ApiSpec
import com.payu.shorturl.model.Url
import play.api.libs.json.{JsObject, Json}

class ShortUrlApiSpec extends ApiSpec {

  private val PATH = "/v1/shorturl"

  private val urlShortened = "1"
  private val urlShortenedNotFound = "2"

  private val createInput = Json.obj(
    "url" -> "https://www.payu.com/"
  )

  "Short Url" when {
    "create" should {

      "return OK if it was registered" in {
        val request = Request(POST, PATH).withInputJson(createInput)
        val response = request.run
        response.status shouldBe OK

        val result = response.contentAs[Url.Result]
        result.urlShortened.length should be.>(0)
      }

      "validate" should {
        def invalidFields(input: JsObject) = {
          val request = Request(POST, PATH).withInputJson(input)
          val response = request.run
          response.status shouldBe BAD_REQUEST
        }

        "return BAD_REQUEST if url is less than 5" in {
          invalidFields(createInput ++ Json.obj("url" -> List.fill(4)("L").mkString))
        }

        "return BAD_REQUEST if url is more than 1024" in {
          invalidFields(createInput ++ Json.obj("url" -> List.fill(1025)("L").mkString))
        }

        "return BAD_REQUEST if url is in wrong format 1" in {
          invalidFields(createInput ++ Json.obj("url" -> "htt://www.payu.com"))
        }

        "return BAD_REQUEST if url is in wrong format 2" in {
          invalidFields(createInput ++ Json.obj("url" -> "httpwww.payu.com"))
        }

        "return BAD_REQUEST if url is in wrong format 3" in {
          invalidFields(createInput ++ Json.obj("url" -> "//www.payu.com"))
        }
      }
    }

    "get" should {

      "return OK if it exists" in {
        val request = Request(GET, PATH + s"/$urlShortened")
        val response = request.run
        response.status shouldBe TEMPORARY_REDIRECT
      }

      "return NOT_FOUND if shortened url does not exists" in {
        val request = Request(GET, PATH + s"/$urlShortenedNotFound")
        val response = request.run
        response.status shouldBe NOT_FOUND
      }
    }
  }
}