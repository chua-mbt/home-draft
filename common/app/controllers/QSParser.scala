package common.controllers

import play.api.mvc._

trait QSParser {
  def paramAsString(
    key: String
  )(
    implicit request: RequestHeader
  ): Option[String] = request.queryString.get(key).flatMap { _.headOption }

  def paramAsLong(
    key: String
  )(implicit request: RequestHeader): Option[Long] = paramAsString(key) map { _.toLong }

  def paramAsInt(
    key: String
  )(implicit request: RequestHeader): Option[Int] = paramAsString(key) map { _.toInt }
}