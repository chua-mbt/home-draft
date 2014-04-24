package common.controllers

import play.api.mvc._
import play.api.mvc.BodyParsers._

import common.models._

trait Security { self: Controller =>
  def user(implicit request: RequestHeader) =
    request.session.get("user_id").flatMap(i => User.findByIdOpt(i.toInt))

  def VisitorAction(
    f: Option[User] => Request[AnyContent] => SimpleResult
  ): Action[AnyContent] = VisitorAction(parse.anyContent)(f)

  def VisitorAction[A](parser: BodyParser[A])(
    f: Option[User] => Request[A] => SimpleResult
  ): Action[A] = Action(parser) { implicit req => f(user)(req) }

  def UserAction(
    f: User => Request[AnyContent] => SimpleResult
  ): Action[AnyContent] = UserAction(parse.anyContent)(f)

  def UserAction[A](parser: BodyParser[A])(
    f: User => Request[A] => SimpleResult
  ): Action[A] = Action(parser) { implicit req =>
    if(!user.isDefined){
      Redirect("/login")
    } else {
      f(user.get)(req)
    }
  }

  def AdminAction(
    f: User => Request[AnyContent] => SimpleResult
  ): Action[AnyContent] = UserAction(parse.anyContent)(f)

  def AdminAction[A](parser: BodyParser[A])(
    f: User => Request[A] => SimpleResult
  ): Action[A] = Action(parser) { implicit req =>
    if(!user.isDefined){
      Redirect("/admin/login")
    } else if(Role.isUserAdmin(user.get)) {
      f(user.get)(req)
    } else {
      Forbidden(views.html.error(
        "Error 403: Forbidden",
        "You do not have the clearance to access this page."
      ))
    }
  }
}