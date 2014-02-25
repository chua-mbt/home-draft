package admin.controllers

import play.api._
import play.api.mvc._

import common.controllers.Secured
import views._

object Application extends Controller with Secured {
  def index = UserAction { user => implicit request =>
    Ok("temp");
    /*
    if(!user.isDefined){
      Redirect(routes.Auth.login)
    }else{
      Ok(html.admin.index("You're authenticated! Now for authorization..."))
    }
    */
  }
}