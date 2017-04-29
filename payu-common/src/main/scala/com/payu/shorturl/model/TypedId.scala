package com.payu.shorturl.model

import scala.annotation.implicitNotFound
import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.reflect.macros.whitebox

trait TypedId extends Any {
  def value: Long

  override def toString: String = value.toString
}

object TypedId {

  @implicitNotFound("No implicit TypedId.Factory defined for ${T}.")
  trait Factory[T <: TypedId] extends (Long => T)

  object Factory {
    implicit def materialize[T <: TypedId]: Factory[T] = macro TypedIdMacro.materializeFactory[T]
  }

  implicit def ShortToId[T <: TypedId](value: Short)(implicit factory: Factory[T]): T = factory(value)

  implicit def IntToId[T <: TypedId](value: Int)(implicit factory: Factory[T]): T = factory(value)

  implicit def LongToId[T <: TypedId](value: Long)(implicit factory: Factory[T]): T = factory(value)

  implicit def TypedIdToLong(id: TypedId): Long = id.value
}

private object TypedIdMacro {

  def materializeFactory[T <: TypedId : c.WeakTypeTag](c: whitebox.Context): c.Tree = {
    import c.universe._

    val allImplicitCandidates = c.openImplicits.map(_.pt.dealias)
    val implicitCandidates = allImplicitCandidates.collect {
      case TypeRef(_, _, toType :: Nil) => toType
      case TypeRef(_, _, _ :: toType :: Nil) => toType
    }.filter(x => x.typeSymbol.isClass && x.typeSymbol.asClass.isCaseClass)

    implicitCandidates match {
      case to :: Nil =>
        q"""new _root_.com.payu.shorturl.model.TypedId.Factory[$to] {
              def apply(id: Long) = new $to(id)
        }"""
      case l => c.abort(NoPosition, s"There are ${l.size} implicit candidates found. There should be only 1.")
    }
  }
}
