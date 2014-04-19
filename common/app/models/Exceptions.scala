package common.models

case class UserNotFound() extends RuntimeException("User not found!")
case class UserAlreadyJoined() extends RuntimeException("Draft not found!")
case class DraftNotFound() extends RuntimeException("Draft not found!")
case class DraftFull() extends RuntimeException("Draft is full!")
case class DraftMinSize() extends RuntimeException("Draft must have participation!")