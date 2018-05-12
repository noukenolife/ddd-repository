package noukenolife.ddd.infrastructure.slick.unitofwork

import noukenolife.ddd.infrastructure.slick.ProfileComponent
import slick.jdbc.JdbcProfile

trait FakeSlickH2TransactionalUOW extends SlickTransactionalUOW with ProfileComponent[JdbcProfile] {
  override val profile = slick.jdbc.H2Profile
}
