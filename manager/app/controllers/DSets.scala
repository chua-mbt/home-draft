package manager.controllers.draft

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.json.Json._

import common.controllers._
import manager.models._
import common.models._

object DSets extends Controller with Security with Pages {
  def drafts = UserAction { user => implicit request =>
    Ok(toJson(Draft.paged(pageParams, user)))
  }

  def draftsByState(name: String) = UserAction { user => implicit request =>
    Ok(toJson(Draft.paged(pageParams, user, Some(name))))
  }

  def states = UserAction { user => implicit request =>
    Ok(toJson(DraftState.list))
  }
}