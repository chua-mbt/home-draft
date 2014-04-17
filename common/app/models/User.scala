package common.models

import play.api.Play.current
import play.api.db.slick.DB

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.{GetResult, StaticQuery}

case class User(id: Long, handle: String, email: String, password: String, roleId: Int)

class UserTable(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Long]("user_id", O.PrimaryKey, O.AutoInc)
  def handle = column[String]("user_handle", O.NotNull)
  def email = column[String]("user_email", O.NotNull)
  def password = column[String]("user_password", O.NotNull)
  def roleId = column[Int]("user_role", O.NotNull)
  def * = (id, handle, email, password, roleId) <> ((User.apply _).tupled, User.unapply)
  def roleIdFK = foreignKey("users_user_role_fkey", roleId, Role.all)(_.id)
}

object User {
  lazy val anonymous = User(-1, "", "", "", -1)
  lazy val all = TableQuery[UserTable]

  def findById(id: Long) = DB.withSession { implicit session =>
    all.filter(_.id === id).firstOption
  }
  def findByHandle(handle: String) = DB.withSession { implicit session =>
    all.filter(_.handle === handle).firstOption
  }

  def add(newUser: User) = DB.withSession { implicit session =>
    all += newUser
  }

  def authenticate(
    handle: String,
    password: String
  ) = DB.withSession { implicit session =>
    StaticQuery.query[(String, String), User]("""
      SELECT * FROM users
      WHERE user_handle = ?
        AND user_password = CRYPT(?, user_password)
    """).firstOption(handle, password)
  }

  implicit val getUserResult = GetResult(r => User(
    r.<<, r.<<, r.<<, r.<<, r.<<
  ))
}