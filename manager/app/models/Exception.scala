package manager.models

case class UserAlreadyJoined() extends RuntimeException("Draft not found!")
case class DraftNotFound() extends RuntimeException("Draft not found!")
case class DStateNotFound() extends RuntimeException("Draft state not found!")
case class DraftFull() extends RuntimeException("Draft is full!")
case class DraftMinSize() extends RuntimeException("Draft must have participation!")
case class DraftLocked() extends RuntimeException("Draft details have been locked!")
case class DraftNotReady() extends RuntimeException("Draft not found!")