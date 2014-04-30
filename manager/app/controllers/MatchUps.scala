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
      Ok(toJson(Match.forRound(hash, round)(user)))
    } catch {
      case e:DraftNotFound => NotFound
    }
  }

  def current(hash: String) = UserAction { user => implicit request =>
    try {
      Ok(toJson(Match.getCurrentRound(hash)(user)))
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

  def edit(hash: String) = UserAction(parse.json) { user => implicit request =>
    try {
      val matches = fromJson[List[Match]](request.body).get.toSet
      Ok(toJson(Match.replaceCurrentRound(hash, matches)(user)))
    } catch {
      case e:UserNotFound => NotFound
      case e:DraftNotFound => NotFound
      case e:InvalidMatchResults => BadRequest
      case e:AssertionError => BadRequest
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