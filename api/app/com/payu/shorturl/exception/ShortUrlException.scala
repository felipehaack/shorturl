package com.payu.shorturl.exception

import com.payu.shorturl.model.{Error, TypedId}

import scala.reflect.ClassTag
import scala.util.control.NoStackTrace

class ShortUrlException(val message: String) extends RuntimeException(message)

object ShortUrlException {

  def systemError(desc: String) = throw new ShortUrlException(desc)

  def notFound[T: ClassTag] = throw new NotFoundFailure(getSimpleName[T])

  def updateFail[T: ClassTag] = throw new UpdateFailure(getSimpleName[T])

  def invalidId[T <: TypedId : ClassTag] = throw new InvalidIdFailure(getSimpleName[T])

  def invalidInput(desc: String) = throw new InvalidInputFailure(desc)

  def invalidInputs(errors: Set[Error]) = throw new InvalidInputsFailure(errors)

  private def getSimpleName[T](implicit ct: ClassTag[T]): String = {
    ct.runtimeClass.getSimpleName
  }
}

class ShortUrlFailure(val code: String) extends ShortUrlException(code) with NoStackTrace

class NotFoundFailure(name: String) extends ShortUrlFailure(s"notFound.${name}")

class InvalidFailure(name: String) extends ShortUrlFailure(s"invalid.${name}")

class InvalidIdFailure(name: String) extends InvalidFailure(s"id.${name}")

class InvalidInputFailure(name: String) extends InvalidFailure(name)

class InvalidInputsFailure(val errors: Set[Error]) extends InvalidFailure("input")

class UpdateFailure(val name: String) extends InvalidFailure(s"update.$name")
