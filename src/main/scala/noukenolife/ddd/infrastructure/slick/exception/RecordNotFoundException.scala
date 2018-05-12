package noukenolife.ddd.infrastructure.slick.exception

case class RecordNotFoundException(message: String = "", cause: Throwable = null) extends Exception(message, cause)