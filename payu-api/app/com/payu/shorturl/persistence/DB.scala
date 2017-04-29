package com.payu.shorturl.persistence

import java.sql.Connection
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.db.Database

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.control.ControlThrowable
import scala.util.{Failure, Success}

@Singleton
class DB @Inject()(
                    db: Database,
                    system: ActorSystem
                  ) {

  private implicit val ec: ExecutionContext = system.dispatchers.lookup("contexts.database")

  def withConnection[A](block: Connection => A): Future[A] = {
    Future(db.withConnection(block))(ec)
  }

  def withTransaction[A](block: Connection => A): Future[A] = {
    Future(db.withTransaction(block))(ec)
  }

  def withTransactionF[A](block: Connection => Future[A]): Future[A] = {
    val p = Promise[A]
    val connection = db.getConnection(false)

    block(connection) onComplete {
      case Success(r) =>
        connection.commit()
        connection.close()
        p.success(r)

      case Failure(e: ControlThrowable) =>
        connection.commit()
        connection.close()
        p.failure(e)

      case Failure(e) =>
        connection.rollback()
        connection.close()
        p.failure(e)
    }

    p.future
  }

}
