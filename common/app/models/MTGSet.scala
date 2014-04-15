package common.models

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.Play.current
import play.api.db.slick.DB

import scala.slick.driver.PostgresDriver.simple._

case class MTGSet(id: String, name: String)

class MTGSetTable(tag: Tag) extends Table[MTGSet](tag, "mtgsets") {
  def id = column[String]("mtgset_id", O.PrimaryKey)
  def name = column[String]("mtgset_name", O.NotNull)
  def * = (id, name) <> ((MTGSet.apply _).tupled , MTGSet.unapply)
}

object MTGSet {
  lazy val src = "https://api.deckbrew.com/mtg/sets"
  lazy val all = TableQuery[MTGSetTable]
  lazy val allSorted = all.sortBy(_.id.asc)

  def list = DB.withSession{ implicit session => allSorted.list }
  def add(newSet: MTGSet) = DB.withTransaction { implicit session =>
    if(!all.filter(_.id === newSet.id).exists.run){ all += newSet }
  }

  implicit object ReadWrite extends Writes[MTGSet] {
    def writes(o: MTGSet) = {
      toJson(Map(
        "id" -> toJson(o.id),
        "name" -> toJson(o.name)
      ))
    }
  }
}