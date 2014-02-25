package common.controllers

import play.api.mvc._
import play.api.mvc.BodyParsers._

import common.models._

trait Secured { self: Controller =>
  def user(implicit request: RequestHeader) =
    request.session.get("user_id").flatMap(i => Users.findById(i.toInt))

  def UserAction(
    f: Option[User] => Request[AnyContent] => SimpleResult
  ): Action[AnyContent] = UserAction(parse.anyContent)(f)

  def UserAction[A](parser: BodyParser[A])(
    f: Option[User] => Request[A] => SimpleResult
  ): Action[A] = {
    Action(parser) { implicit req => f(user)(req) }
  }

/*
  def withAccessControl(
    f: User => Request[AnyContent] => SimpleResult
  ): Action[AnyContent] = accessControlled(parse.anyContent)(f)

  def withAccessControl(parser: BodyParser[A], role: String)(
    f: User => Request[A] => SimpleResult
  ): Action[A] = {
    Action(parser) { implicit req =>
      if(!user.isDefined){
        Redirect("/")
      } else {
        f(user.get)(req)
      }
    }
  }
*/
}