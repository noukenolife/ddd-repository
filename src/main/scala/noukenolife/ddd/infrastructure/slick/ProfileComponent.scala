package noukenolife.ddd.infrastructure.slick

import slick.basic.BasicProfile

trait ProfileComponent[P <: BasicProfile] {
  val profile: P
}
