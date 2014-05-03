package manager.controllers.draft

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Json._

import common.controllers._
import common.models._
import manager.models._
import manager.exceptions._

object DStates extends Controller with Security {
  def next(hash: String) = UserAction { user => implicit request =>
    try{
      Ok(toJson(Draft.nextState(hash)(user)))
    } catch {
      case e:DraftNotFound => NotFound
      case e:DraftNotReady => BadRequest
      case e:IncompleteRecords => BadRequest
    }
  }

  def previous(hash: String) = UserAction { user => implicit request =>
    try{
      Ok(toJson(Draft.previousState(hash)(user)))
    } catch {
      case e:DraftNotFound => NotFound
    }
  }

  def abort(hash: String) = UserAction { user => implicit request =>
    try{
      Ok(toJson(Draft.abort(hash)(user)))
    } catch {
      case e:DraftNotFound => NotFound
      case e:DraftLocked => BadRequest
    }
  }
}