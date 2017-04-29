package api

import javax.inject.Inject

import com.payu.shorturl.model.Error
import com.payu.shorturl.util.Logging
import play.api.i18n._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

class Api extends Controller
  with JsonSupport with I18nSupport
  with ImplicityHelpers with Logging {
  @Inject var messagesApi: MessagesApi = _

  protected val Action = play.api.mvc.Action
}

trait ImplicityHelpers {
  @Inject implicit protected var execution: ExecutionContext = _
}

trait JsonSupport extends ImplicityHelpers {
  self: Controller with I18nSupport =>

  implicit class ResultAsJson(status: Status)(implicit requestHeader: RequestHeader) {
    def asJson[T: Writes](o: T): Result = o match {
      case true => Ok
      case false | None => NotFound
      case () => status
      case _ => status(Json.toJson(o))
    }

    def asJson[T: Writes](f: Future[T]): Future[Result] = {
      f.map(r => asJson(r))
    }
  }

  object json {
    private val InvalidCode = "input.invalid"

    def apply[T: Reads]: BodyParser[T] = {
      BodyParser("json input") { implicit request =>
        parse.json(request).mapFuture {
          case Left(simpleResult) =>
            Future.successful(Left(simpleResult))
          case Right(jsValue) =>
            jsValue.validate match {
              case JsSuccess(v, _) => Future.successful(Right(v))
              case e: JsError =>
                val errors = JsError.toFlatForm(e).flatMap {
                  case (code, error) => error.map(e => Error(code, e.message))
                }
                ErrorRequest[T](errors.toSet)
            }
        }
      }
    }

    private def ErrorRequest[T](errors: Set[Error]): Future[Either[Result, T]] = {
      val error = Error(InvalidCode, Messages(InvalidCode), Some(errors))
      val result = BadRequest(Json.toJson(error))
      Future.successful(Left(result))
    }
  }

}

