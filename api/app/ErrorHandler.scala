import javax.inject._

import com.payu.shorturl.model.Error
import com.payu.shorturl.util.Logging
import play.api._
import play.api.http.DefaultHttpErrorHandler
import play.api.http.Status._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{RequestHeader, Result}
import play.api.routing.Router

import scala.concurrent.{Future, TimeoutException}
import scala.util.control.NonFatal

@Singleton
class ErrorHandler @Inject()(
                              env: Environment,
                              config: Configuration,
                              sourceMapper: OptionalSourceMapper,
                              router: Provider[Router],
                              val messagesApi: MessagesApi
                            ) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) with Logging with I18nSupport {

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = statusCode match {
    case BAD_REQUEST => onBadRequest(request, message)
    case FORBIDDEN => onForbidden(request, message)
    case NOT_FOUND => onNotFound(request, message)
    case clientError if statusCode >= 400 && statusCode < 500 => ErrorResult(Status(statusCode), message)
    case nonClientError =>
      throw new IllegalArgumentException(s"onClientError invoked with non client error status code $statusCode: $message")
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    exception match {
      case ex: TimeoutException => ErrorResult(RequestTimeout, "server.timeout")
      case ex: NotImplementedError => ErrorResult(NotImplemented, "server.not_implemented")
      case NonFatal(ex) =>
        logger.error(s"Request:$request", ex)
        ErrorResult(InternalServerError, "server.internal.error")
    }
  }

  override protected def onNotFound(request: RequestHeader, message: String): Future[Result] = {
    ErrorResult(NotFound, message)
  }

  override protected def onForbidden(request: RequestHeader, message: String): Future[Result] = {
    ErrorResult(Forbidden, message)
  }

  override protected def onBadRequest(request: RequestHeader, message: String): Future[Result] = {
    ErrorResult(BadRequest, message)
  }

  private def ErrorResult(status: Status, code: String, errors: Option[Set[Error]] = None) = {
    val error = Error(code, Messages(code), errors)
    val result = status(Json.toJson(error))
    Future.successful(result)
  }

}
