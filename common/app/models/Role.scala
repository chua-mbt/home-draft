package common.models

import scala.slick.driver.PostgresDriver.simple._

case class Role(id: Int, name: String)

class RoleTable(tag: Tag) extends Table[Role](tag, "roles") {
  def id = column[Int]("role_id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("role_name", O.NotNull)
  def * = (id, name) <> (Role.tupled, Role.unapply)
}

object Roles extends SlickPGModel{
  lazy val all = TableQuery[RoleTable]
  def findById(id: Int) = dbConn withSession { implicit session =>
    all.filter(_.id === id).firstOption
  }
  def findByUser(user: User) = findById(user.roleId)
  def isUserAdmin(user: User) = {
    val role = findById(user.roleId)
    role.isDefined && role.get.name == "admin"
  }
}