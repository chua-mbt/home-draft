package manager.controllers

import java.sql.Timestamp
import org.joda.time.DateTime

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
      "hash" -> optional(text),
      "start" -> jodaDate(Draft.jodaTSFormat),
      "set1" -> nonEmptyText,
      "set2" -> nonEmptyText,
      "set3" -> nonEmptyText,
      "venue" -> optional(text),
      "food" -> optional(text),
      "fee" -> optional(of[Float]),
      "details" -> optional(text)
    ){case (hash, start, set1, set2, set3, venue, food, fee, details) => Draft(
      hash.getOrElse(""), new Timestamp(start.getMillis()),
      set1, set2, set3, 1,
      venue, food, fee, details
    )}{(draft:Draft) => Some((
      Some(draft.hash), new DateTime(draft.start),
      draft.set1, draft.set2, draft.set3,
      draft.venue, draft.food, draft.fee, draft.details
    ))}
  )

  def make = UserAction(parse.json) { user => implicit request =>
    draftForm.bindFromRequest.fold(
      { case errors =>
        play.Logger.debug("TERRIBAD")
      },
      { case newDraft =>
        Draft.add(newDraft, user.handle)
      }
    )
    Respond()
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