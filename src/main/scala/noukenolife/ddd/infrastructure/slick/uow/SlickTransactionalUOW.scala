package noukenolife.ddd.infrastructure.slick.unitofwork

import noukenolife.ddd.application.api.UnitOfWork
import noukenolife.ddd.domain.api.lifecycle.IOContext
import noukenolife.ddd.infrastructure.slick.ProfileComponent
import noukenolife.ddd.infrastructure.slick.lifecycle.SlickIOContext
import slick.jdbc.{JdbcBackend, JdbcProfile}

import scala.concurrent.Future

trait SlickTransactionalUOW extends UnitOfWork[slick.dbio.DBIO, Future] {
  this: ProfileComponent[JdbcProfile] =>

  import profile.api._

  def db: JdbcBackend#Database

  override def run[R](f: IOContext => DBIO[R]): Future[R] = {
    db.run(f(SlickIOContext).transactionally)
  }
}
