package common.controllers

import play.api.mvc._
import play.api.mvc.Results._
import play.api.libs.json._
import play.api.libs.json.Json._

trait API {
  def Respond(
    response: JsObject
  ): SimpleResult = Ok(response + ("success" -> toJson(true)))

  def Respond(
    response: (String, JsValue)*
  ): SimpleResult = Ok(toJson(Map("success" -> toJson(true)) ++ response))
}
