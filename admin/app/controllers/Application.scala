package admin.controllers

import play.api._
import play.api.mvc._

import common.controllers.Security
import views._

object Application extends Controller with Security {
  def index = VisitorAction { user => implicit request =>
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