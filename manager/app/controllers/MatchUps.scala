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

object MatchUps extends Controller with Security {
  def forRound(hash: String, round: Int) = UserAction { user => implicit request =>
    try {
      Ok(toJson("OK"))
    } catch {
      case e:DraftNotFound => NotFound
    }
  }

  def current(hash: String) = UserAction { user => implicit request =>
    try {
      Ok(toJson("OK"))
    } catch {
      case e:DraftNotFound => NotFound
    }
  }

  def next(hash: String) = UserAction { user => implicit request =>
    try {
      Ok(toJson("OK"))
    } catch {
      case e:DraftNotFound => NotFound
    }
  }

  def edit(hash: String) = UserAction { user => implicit request =>
    try {
      Ok(toJson("OK"))
    } catch {
      case e:DraftNotFound => NotFound
    }
  }

  def cancel(hash: String) = UserAction { user => implicit request =>
    try {
      Ok(toJson("OK"))
    } catch {
      case e:DraftNotFound => NotFound
    }
  }

  def standings(hash: String) = UserAction { user => implicit request =>
    try {
      Ok(toJson("OK"))
    } catch {
      case e:DraftNotFound => NotFound
    }
  }
}