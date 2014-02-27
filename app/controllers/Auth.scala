package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import common.controllers.Secured
import common.models._
import views._

object Auth extends Controller with Secured {
  val loginForm = Form(
    mapping(
      "handle" -> nonEmptyText,
      "password" -> nonEmptyText
    ) {
      (handle, password) => Users.authenticate(handle, password)
    } {
      (userOpt:Option[User]) => Some(
        (userOpt.getOrElse(Users.anonymous).handle, "")
      )
    }
  )

  def login = VisitorAction { user => implicit request =>
    if(user.isDefined) {
      Redirect(routes.Application.index)
    } else {
      Ok(html.login(loginForm))
    }
  }

  def logout = Action {
    Results.Redirect(routes.Auth.login).withNewSession
  }

  def authenticate = VisitorAction(parse.urlFormEncoded) { user => implicit request =>
    val form = loginForm.bindFromRequest
    form.fold(
      errors => BadRequest(html.login(errors)),
      {
        case Some(User(id, _, _, _, _)) => {
          Results.Redirect(routes.Application.index).withSession(
            "user_id" -> id.toString
          )
        }
        case _ => BadRequest(html.login(form))
      }
    )
  }
}