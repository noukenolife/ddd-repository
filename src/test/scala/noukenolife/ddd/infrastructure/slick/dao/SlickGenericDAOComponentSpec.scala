package noukenolife.ddd.infrastructure.slick.dao

import noukenolife.ddd.infrastructure.slick.exception.RecordNotFoundException
import noukenolife.ddd.infrastructure.slick.record._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import slick.jdbc.{H2Profile, JdbcBackend}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class SlickGenericDAOComponentSpec extends WordSpec with Matchers with BeforeAndAfterAll with ScalaFutures {

  val db: JdbcBackend#Database = JdbcBackend.Database.forURL(
    url = "jdbc:h2:mem:slick_ddd_repository_test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
    driver = "org.h2.Driver"
  )
  import slick.jdbc.H2Profile.api._

  val comp = new FakeSlickH2DAOComponent {
    val dao = new FakeSlickH2DAO
  }

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

  "A SlickGenericDAOComponent" must {
    "has a profile" in {
      comp.profile shouldBe a[H2Profile]
    }
    "insert a new record" in {
      val record = FakeRecord(1l, "Value")

      whenReady(db.run(comp.dao.insertOrUpdate(record))) {
        _ shouldEqual 1
      }

      whenReady(db.run(comp.dao.findById(1l))) {
        _ shouldEqual record
      }
    }
    "update a record" in {
      val record = FakeRecord(1l, "New Value")

      whenReady(db.run(comp.dao.insertOrUpdate(record))) {
        _ shouldEqual 1
      }

      whenReady(db.run(comp.dao.findById(1l))) {
        _ shouldEqual record
      }
    }
    "delete a record" in {
      whenReady(db.run(comp.dao.delete(1l))) {
        _ shouldEqual 1
      }

      whenReady(db.run(comp.dao.findById(1l)).failed) { e =>
        e shouldBe a[RecordNotFoundException]
      }
    }
  }

  override protected def afterAll(): Unit = {
    db.close()
  }
}
