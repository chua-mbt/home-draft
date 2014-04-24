package common.models

case class UserNotFound() extends RuntimeException("User not found!")