package noukenolife.ddd.infrastructure.slick.lifecycle

import noukenolife.ddd.domain.api.lifecycle.{EntityNotFoundException, IOContext, Repository, RepositoryException}
import noukenolife.ddd.domain.api.model.{Entity, Id}
import noukenolife.ddd.infrastructure.api.Record
import noukenolife.ddd.infrastructure.slick.ProfileComponent
import noukenolife.ddd.infrastructure.slick.dao.SlickGenericDAOComponent
import noukenolife.ddd.infrastructure.slick.exception.RecordNotFoundException
import slick.dbio.DBIO
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait SlickGenericRepository[K, R <: Record[K], I <: Id[K], E <: Entity[I]]
  extends Repository[I, E, DBIO] {
  this: SlickGenericDAOComponent with ProfileComponent[JdbcProfile] =>

  def dao: SlickGenericDAO[K, R, _]

  implicit def ec: ExecutionContext
  implicit def toRecord(entity: E): R
  implicit def toEntity(record: R): E

  override def resolve(id: I)(implicit ctx: IOContext): DBIO[E] = {
    dao.findById(id.value).asTry.map {
      case Success(r) => toEntity(r)
      case Failure(e) => e match {
        case _: RecordNotFoundException => throw EntityNotFoundException(cause = e)
        case _ => throw RepositoryException(cause = e)
      }
    }
  }

  override def store(entity: E)(implicit ctx: IOContext): DBIO[Unit] = {
    dao.insertOrUpdate(toRecord(entity)).asTry.map {
      case Success(r) => Unit
      case Failure(e) => throw RepositoryException(cause = e)
    }
  }

  override def delete(id: I)(implicit ctx: IOContext): DBIO[Unit] = {
    dao.delete(id.value).asTry.map {
      case Success(r) => Unit
      case Failure(e) => throw RepositoryException(cause = e)
    }
  }
}
