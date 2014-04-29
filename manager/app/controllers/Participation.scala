package manager.controllers.draft

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Json._

import common.controllers._
import common.models._
import common.exceptions._
import manager.models._
import manager.exceptions._

object Participation extends Controller with Security {
  def participants(hash: String) = UserAction { user => implicit request =>
    try {
      Ok(toJson(Participant.forDraft(hash)(user)))
    } catch {
      case e:DraftNotFound => NotFound
    }
  }

  def add(
    hash: String
  ) = UserAction(parse.json) { user => implicit request =>
    try {
      val handle = (request.body \ "handle").as[String]
      Ok(toJson(Participant.add(hash, handle)(user)))
    } catch {
      case e:UserNotFound => NotFound
      case e:UserAlreadyJoined => BadRequest
      case e:DraftNotFound => NotFound
      case e:DraftFull => BadRequest
    }
  }

  def remove(
    hash: String, handle: String
  ) = UserAction { user => implicit request =>
    try {
      Ok(toJson(Participant.remove(hash, handle)(user)))
    } catch {
      case e:UserNotFound => NotFound
      case e:DraftNotFound => NotFound
      case e:DraftMinSize => BadRequest
    }
  }
}