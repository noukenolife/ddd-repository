package noukenolife.ddd.infrastructure.slick.dao

import noukenolife.ddd.infrastructure.slick.ProfileComponent
import slick.jdbc.JdbcProfile
import noukenolife.ddd.infrastructure.slick.record._

trait FakeSlickH2DAOComponent extends SlickGenericDAOComponent with ProfileComponent[JdbcProfile] {
  override val profile: JdbcProfile = slick.jdbc.H2Profile

  import profile.api._

  class FakeRecords(tag: Tag) extends Table[FakeRecord](tag, "fake_records") {
    def id = column[Long]("id", O.PrimaryKey)
    def value = column[String]("value")
    override def * = (id, value) <> (FakeRecord.tupled, FakeRecord.unapply)
  }

  class FakeSlickH2DAO extends SlickGenericDAO[Long, FakeRecord, FakeRecords] {
    override def rows: TableQuery[FakeRecords] = TableQuery[FakeRecords]
    override def rowId(t: FakeRecords): Rep[Long] = t.id
  }
}
