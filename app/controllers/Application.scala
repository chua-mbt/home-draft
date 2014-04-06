package controllers

import play.api._
import play.api.mvc._

import common.controllers.Security
import views._

object Application extends Controller with Security {
  def index = UserAction { user => implicit request =>
    Redirect(routes.Application.userView(user.handle, ""))
  }

  def userView(
    handle: String,
    file: String = ""
  ) = UserAction { user => implicit request =>
    Ok(html.index()(user))
  }
}