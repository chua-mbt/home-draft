package manager.controllers

import java.sql.Timestamp

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._

import play.api.libs.json._
import play.api.libs.json.Json._

import common.controllers._
import manager.models._

object DraftCon extends Controller with Security with API with Pages {
  val draftForm = Form(
    mapping(
      "start" -> of[Long],
      "venue" -> optional(text),
      "food" -> optional(text),
      "fee" -> optional(of[Float]),
      "set1" -> optional(text),
      "set2" -> optional(text),
      "set3" -> optional(text)
    ){case (start, venue, food, fee, set1, set2, set3) => Draft(
      "", new Timestamp(start), 1,
      venue, food, fee, set1, set2, set3
    )}{(draft:Draft) => Some((
        draft.start.getTime(), draft.venue, draft.food,
        draft.fee, draft.set1, draft.set2, draft.set3
    ))}
  )

  def create = UserAction(parse.json) { user => implicit request =>
    Respond("results" -> toJson(""))
  }

  def mod(hash:String) = UserAction(parse.json) { user => implicit request =>
    Respond("results" -> toJson(hash))
  }

  def drafts = UserAction { user => implicit request => {
    Respond("results" -> toJson(Draft.paged(pageParams)))
  }}

  def draft(hash:String) = UserAction { user => implicit request => {
    Respond("results" -> toJson(Draft.findByHash(hash)))
  }}
}