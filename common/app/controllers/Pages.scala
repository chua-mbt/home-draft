package common.controllers

import common.models.PageParam
import play.api.mvc._
import play.api.mvc.BodyParsers._

trait Pages extends QSParser {
  def DEFAULT_START:Int = 0
  def DEFAULT_COUNT:Int = 10

  def pageParams(implicit request: RequestHeader) = {
    PageParam(
      paramAsInt("start").getOrElse(DEFAULT_START),
      paramAsInt("count").getOrElse(DEFAULT_COUNT)
    )
  }
}
