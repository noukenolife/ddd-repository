package noukenolife.ddd.infrastructure.slick.unitofwork

import noukenolife.ddd.domain.api.lifecycle.RepositoryException
import noukenolife.ddd.domain.api.model.{FakeEntity, FakeId}
import noukenolife.ddd.infrastructure.slick.lifecycle.FakeSlickH2Repository
import org.scalatest.{AsyncWordSpec, BeforeAndAfterAll, Matchers}
import slick.jdbc.JdbcBackend

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SlickTransactionalUOWSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  val _db = JdbcBackend.Database.forURL(
    url = "jdbc:h2:mem:slick_ddd_repository_test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
    driver = "org.h2.Driver"
  )

  val uow: SlickTransactionalUOW = new FakeSlickH2TransactionalUOW {
    override val db = _db
  }
  val repo = new FakeSlickH2Repository

  import slick.jdbc.H2Profile.api._

  override protected def beforeAll(): Unit = {
    val createTable =
      sqlu"""
            CREATE TABLE IF NOT EXISTS `fake_records` (
              `id` BIGINT(20) NOT NULL PRIMARY KEY,
              `value` VARCHAR(255) NOT NULL
            )
        """
    Await.result(_db.run(createTable), Duration.Inf)
  }

  "SlickTransactionalUOW" must {
    "run" in {
      def run = uow.run { implicit ctx =>
        val entity = FakeEntity(FakeId(1l), "Value")

        for {
          _ <- repo.store(entity)
          e1 <- repo.resolve(FakeId(1l))
          _ <- repo.store(e1.copy(value = "New Value"))
          e2 <- repo.resolve(FakeId(1l))
          _ <- repo.delete(FakeId(1l))
        } yield e2
      }

      run.map(_.value shouldEqual "New Value")
      _db.run(repo.dao.rows.result).map(_ shouldEqual Seq.empty)
    }

    "rollback on failure" in {
      def run = uow.run { implicit ctx =>
        val entity1 = FakeEntity(FakeId(1l), "Value1")
        val entity2 = FakeEntity(FakeId(2l), "Value2")

        for {
          _ <- repo.store(entity1)
          _ <- repo.store(entity2)
          _ <- DBIO.failed(RepositoryException())
        } yield ()
      }

      recoverToSucceededIf[RepositoryException](run)
      _db.run(repo.dao.rows.result).map(_ shouldEqual Seq.empty)
    }
  }

  override protected def afterAll(): Unit = {
    _db.close()
  }
}
