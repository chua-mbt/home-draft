package controllers

import play.api._
import play.api.mvc._

import common.controllers.Secured
import views._

object Application extends Controller with Secured {
  def index = UserAction { user => implicit request =>
    Redirect(routes.Application.userView(user.handle))
  }

  def userView(
    handle: String
  ) = UserAction { user => implicit request =>
    Ok(html.index()(user))
  }
}