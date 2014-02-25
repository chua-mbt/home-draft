package common.models

import scala.slick.driver.PostgresDriver.simple._

case class Role(id: Long, name: String)

class RoleTable(tag: Tag) extends Table[Role](tag, "roles") {
  def id = column[Long]("role_id", O.PrimaryKey)
  def name = column[String]("role_name")
  def * = (id, name) <> (Role.tupled, Role.unapply)
}

object Roles extends SlickPGModel{
  lazy val all = TableQuery[RoleTable]
  def findById(id: Long) = dbConn withSession { implicit session =>
    all.filter(_.id === id).firstOption
  }
  def findByUser(user: User) = findById(user.id)
}