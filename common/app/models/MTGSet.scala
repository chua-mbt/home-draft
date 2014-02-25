package common.models

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.StaticQuery

case class MTGSet(id: String, name: String)

class MTGSetTable(tag: Tag) extends Table[MTGSet](tag, "mtgsets") {
  def id = column[String]("mtgset_id", O.PrimaryKey)
  def name = column[String]("mtgset_name")
  def * = (id, name) <> (MTGSet.tupled, MTGSet.unapply)
}

object MTGSets extends SlickPGModel{
  lazy val all = TableQuery[MTGSetTable]
  def add(newSet: MTGSet) = dbConn withTransaction { implicit session =>
    if(!all.filter(_.id === newSet.id).exists.run){ all += newSet }
  }
}