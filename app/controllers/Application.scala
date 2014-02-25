package controllers

import play.api._
import play.api.mvc._

import common.controllers.Secured
import views._

object Application extends Controller with Secured {
  def index = UserAction { user => implicit request =>
    if(!user.isDefined){
      Redirect(routes.Auth.login)
    }else{
      Ok(html.index()(user.get))
    }
  }
}