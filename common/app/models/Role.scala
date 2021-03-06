package common.models

import common.exceptions._

import play.api.Play.current
import play.api.db.slick.DB

import scala.slick.driver.PostgresDriver.simple._

case class Role(id: Int, name: String)

class RoleTable(tag: Tag) extends Table[Role](tag, "roles") {
  def id = column[Int]("role_id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("role_name", O.NotNull)
  def * = (id, name) <> ((Role.apply _).tupled, Role.unapply)
}

object Role extends HomeDraftModel {
  lazy val all = TableQuery[RoleTable]
  def findById(id: Int) = DB.withSession { implicit session =>
    extract(
      all.filter(_.id === id).take(1).firstOption,
      NotFound("Role not found!")
    )
  }
  def findByUser(user: User) = findById(user.roleId)
  def isUserAdmin(user: User) = {
    val role = findById(user.roleId)
    role.name == "admin"
  }
}