package common.exceptions

case class NotFound(msg: String) extends RuntimeException(msg)
case class UserNotFound() extends RuntimeException("User not found!")