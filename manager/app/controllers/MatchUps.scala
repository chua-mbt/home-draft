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
  def rounds(hash: String) = UserAction { user => implicit request =>
    try {
      Ok(Json.obj(
        "rounds" -> toJson(Match.rounds(hash)(user))
      ))
    } catch {
      case e:DraftNotFound => NotFound
    }
  }

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

  def edit(hash: String) = UserAction(parse.json) { user => implicit request =>
    try {
      val matches = fromJson[List[Match]](request.body).get.toSet
      Ok(toJson(Match.replaceCurrentRound(hash, matches)(user)))
    } catch {
      case e:UserNotFound => NotFound
      case e:DraftNotFound => NotFound
      case e:InvalidRecord => BadRequest
      case e:AssertionError => BadRequest
    }
  }

  def next(hash: String) = UserAction(parse.json) { user => implicit request =>
    try {
      val matches = fromJson[List[Match]](request.body).get.toSet
      Ok(toJson(Match.makeNewRound(hash, matches)(user)))
    } catch {
      case e:DraftNotFound => NotFound
      case e:IncompleteRecords => BadRequest
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
      val results = Match.standings(hash)(user)
      val rounds = results._1 map {
        case (round, records) =>
          (
            round.toString,
            records map { toJson(_)(Match.RecordRWFormat) }
          )
      }
      val cumulative = results._2 map {
        toJson(_)(Match.RecordRWFormat)
      }
      Ok(Json.obj(
        "rounds" -> toJson(rounds),
        "cumulative" -> toJson(cumulative)
      ))
    } catch {
      case e:DraftNotFound => NotFound
      case e:IncompleteRecords => BadRequest
    }
  }
}