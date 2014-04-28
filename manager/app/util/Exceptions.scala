package manager.exceptions

import common.exceptions._

case class UserAlreadyJoined() extends RuntimeException("User already joined!")
case class DraftNotFound() extends RuntimeException("Draft not found!")
case class DStateNotFound() extends RuntimeException("Draft state not found!")
case class DraftFull() extends RuntimeException("Draft is full!")
case class DraftMinSize() extends RuntimeException("Draft must have participation!")
case class DraftLocked() extends RuntimeException("Draft details have been locked!")
case class DraftNotReady() extends RuntimeException("Draft not ready!")
case class InvalidMatchResults() extends RuntimeException("Invalid match results!")