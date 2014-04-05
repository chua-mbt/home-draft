package common.models

import play.api.Play.current
import play.api.db.slick.DB

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.StaticQuery

case class MTGSet(id: String, name: String)

class MTGSetTable(tag: Tag) extends Table[MTGSet](tag, "mtgsets") {
  def id = column[String]("mtgset_id", O.PrimaryKey)
  def name = column[String]("mtgset_name", O.NotNull)
  def * = (id, name) <> ((MTGSet.apply _).tupled , MTGSet.unapply)
}

object MTGSet {
  lazy val all = TableQuery[MTGSetTable]
  lazy val allSorted = all.sortBy(_.id.asc)
  def add(newSet: MTGSet) = DB.withTransaction { implicit session =>
    if(!all.filter(_.id === newSet.id).exists.run){ all += newSet }
  }
}