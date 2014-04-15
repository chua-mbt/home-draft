package common.controllers

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.json.Json._

import common.models._

object MTGSetCon extends Controller {
  def mtgsets = Action { implicit request => {
    Ok(toJson(MTGSet.list))
  }}
}