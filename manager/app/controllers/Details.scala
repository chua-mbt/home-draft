package manager.controllers.draft

import java.sql.Timestamp
import java.util.Date

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.libs.json._
import play.api.libs.json.Json._

import common.controllers._
import manager.models._
import common.models._

object Details extends Controller with Security {
  val draftForm = Form(
    mapping(
      "hash" -> optional(text),
      "start" -> date(Draft.tsFormat),
      "set1" -> nonEmptyText,
      "set2" -> nonEmptyText,
      "set3" -> nonEmptyText,
      "venue" -> optional(text),
      "food" -> optional(text),
      "fee" -> optional(of[Float]),
      "details" -> optional(text)
    ){case (hash, start, set1, set2, set3, venue, food, fee, details) => Draft(
      hash.getOrElse(""), new Timestamp(start.getTime),
      set1, set2, set3,
      DraftState.findByName("upcoming").number,
      venue, food, fee, details
    )}{(draft:Draft) => Some((
      Some(draft.hash), new Date(draft.start.getTime),
      draft.set1, draft.set2, draft.set3,
      draft.venue, draft.food, draft.fee, draft.details
    ))}
  )

  def make = UserAction(parse.json) { user => implicit request =>
    draftForm.bindFromRequest.fold(
      errors => BadRequest(errors.toString),
      newDraft => {
        val hash = Draft.add(newDraft, user)
        Ok(toJson(Map("hash" -> toJson(hash))))
      }
    )
  }

  def edit(hash:String) = UserAction(parse.json) { user => implicit request =>
    draftForm.bindFromRequest.fold(
      errors => BadRequest(errors.toString),
      draft => {
        try {
          assert(hash == draft.hash)
          Draft.edit(draft, user)
          Ok(toJson(Map("hash" -> toJson(hash))))
        } catch {
          case e:DraftNotFound => NotFound
          case e:AssertionError => BadRequest
        }
      }
    )
  }

  def draft(hash:String) = UserAction { user => implicit request =>
    Ok(toJson(Draft.findByHash(hash, user)))
  }
}