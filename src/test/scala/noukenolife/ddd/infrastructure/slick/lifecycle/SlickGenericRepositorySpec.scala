package noukenolife.ddd.infrastructure.slick.lifecycle

import noukenolife.ddd.domain.api.lifecycle.{EntityNotFoundException, FakeIOContext, IOContext, RepositoryException}
import noukenolife.ddd.domain.api.model.{FakeEntity, FakeId}
import noukenolife.ddd.infrastructure.slick.record.FakeRecord
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import slick.jdbc.JdbcBackend

import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class SlickGenericRepositorySpec extends WordSpec with Matchers with BeforeAndAfterAll with ScalaFutures with MockFactory {

  val db: JdbcBackend#Database = JdbcBackend.Database.forURL(
    url = "jdbc:h2:mem:slick_ddd_repository_test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
    driver = "org.h2.Driver"
  )
  import slick.jdbc.H2Profile.api._

  val repo = new FakeSlickH2Repository {
    type DAO = FakeSlickH2DAO
  }

  val errorRepo = new FakeSlickH2Repository {
    override val dao: FakeSlickH2DAO = stub[FakeSlickH2DAO]
  }

  implicit val ctx: IOContext = FakeIOContext

  override protected def beforeAll(): Unit = {
    val createTable =
      sqlu"""
            CREATE TABLE IF NOT EXISTS `fake_records` (
              `id` BIGINT(20) NOT NULL PRIMARY KEY,
              `value` VARCHAR(255) NOT NULL
            )
        """
    Await.result(db.run(createTable), Duration.Inf)
  }

  "A SlickGenericRepository" must {
    "has a generic dao" in {
      repo.dao shouldBe a[repo.DAO]
    }
    "store a new entity" in {
      val entity = FakeEntity(FakeId(1l), "Value")

      whenReady(db.run(repo.store(entity)))(_ => succeed)

      whenReady(db.run(repo.resolve(FakeId(1l)))) {
        _ shouldEqual entity
      }
    }
    "update an entity" in {
      val entity = FakeEntity(FakeId(1l), "New Value")

      whenReady(db.run(repo.store(entity)))(_ => succeed)

      whenReady(db.run(repo.resolve(FakeId(1l)))) {
        _ shouldEqual entity
      }
    }
    "delete an entity" in {
      whenReady(db.run(repo.delete(FakeId(1l))))(_ => succeed)

      whenReady(db.run(repo.resolve(FakeId(1l))).failed) {
        _ shouldBe a[EntityNotFoundException]
      }
    }
    "throw repository exception" in {
      val entity = FakeEntity(FakeId(1l), "Value")

      (errorRepo.dao.findById(_: Long)(_: ExecutionContext))
        .when(*, *)
        .returning(DBIO.failed(new Exception()))

      (errorRepo.dao.insertOrUpdate _)
        .when(*)
        .returning(DBIO.failed(new Exception()))

      (errorRepo.dao.delete _)
        .when(*)
        .returning(DBIO.failed(new Exception()))

      whenReady(db.run(errorRepo.resolve(FakeId(1l))).failed) {
        _ shouldBe a[RepositoryException]
      }

      whenReady(db.run(errorRepo.store(entity)).failed) {
        _ shouldBe a[RepositoryException]
      }

      whenReady(db.run(errorRepo.delete(FakeId(1l))).failed) {
        _ shouldBe a[RepositoryException]
      }
    }
  }

  override protected def afterAll(): Unit = {
  }
}
