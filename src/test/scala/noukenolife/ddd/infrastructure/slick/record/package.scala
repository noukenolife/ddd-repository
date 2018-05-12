package noukenolife.ddd.infrastructure.slick

import noukenolife.ddd.infrastructure.api.Record

package object record {

  case class FakeRecord(id: Long, value: String) extends Record[Long]
}
