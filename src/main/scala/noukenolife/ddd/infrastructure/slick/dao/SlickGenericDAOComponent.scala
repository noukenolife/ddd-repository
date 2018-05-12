package noukenolife.ddd.infrastructure.slick.dao

import noukenolife.ddd.infrastructure.api.Record
import noukenolife.ddd.infrastructure.slick.ProfileComponent
import noukenolife.ddd.infrastructure.slick.exception.RecordNotFoundException
import slick.ast.BaseTypedType
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

trait SlickGenericDAOComponent {
  this: ProfileComponent[JdbcProfile] =>

  import profile.api._

  abstract class SlickGenericDAO[I: BaseTypedType, R <: Record[I], T <: Table[R]](implicit m: Manifest[R]) {
    def rows: TableQuery[T]
    def rowId(t: T): Rep[I]
    def findById(id: I)(implicit ec: ExecutionContext): DBIO[R] = {
      rows.filter(t => rowId(t) === id)
        .result
        .headOption
        .map(_.getOrElse(throw RecordNotFoundException(s"${m.runtimeClass.getCanonicalName} with ID $id is not found.")))
    }
    def insertOrUpdate(record: R): DBIO[Int] = rows.insertOrUpdate(record)
    def delete(id: I): DBIO[Int] = rows.filter(t => rowId(t) === id).delete
  }
}
