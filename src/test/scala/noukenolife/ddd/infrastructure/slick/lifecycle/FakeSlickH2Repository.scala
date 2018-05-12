package noukenolife.ddd.infrastructure.slick.lifecycle

import noukenolife.ddd.domain.api.model.{FakeEntity, FakeId}
import noukenolife.ddd.infrastructure.slick.dao.FakeSlickH2DAOComponent
import noukenolife.ddd.infrastructure.slick.record.FakeRecord

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

class FakeSlickH2Repository()(implicit override val ec: ExecutionContext)
  extends SlickGenericRepository[Long, FakeRecord, FakeId, FakeEntity] with FakeSlickH2DAOComponent {

  override val dao = new FakeSlickH2DAO
  override implicit def toRecord(entity: FakeEntity): FakeRecord = {
    FakeRecord(entity.id.value, entity.value)
  }
  override implicit def toEntity(record: FakeRecord): FakeEntity = {
    FakeEntity(FakeId(record.id), record.value)
  }
}
