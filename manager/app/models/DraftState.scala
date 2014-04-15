package manager.models

import common.models._

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.Play.current
import play.api.db.slick.DB

import scala.slick.driver.PostgresDriver.simple._

case class DraftState(number: Int, name: String)

class DraftStateTable(tag: Tag) extends Table[DraftState](tag, "dstates") {
  def number = column[Int]("dstate_number", O.PrimaryKey)
  def name = column[String]("dstate_name", O.NotNull)
  def * = (number, name) <> ((DraftState.apply _).tupled, DraftState.unapply)
}

object DraftState {
  lazy val all = TableQuery[DraftStateTable]
  lazy val allSorted = all.sortBy(_.number.asc)
  def list = DB.withSession { implicit session =>
    allSorted.list
  }
  def findByNumber(number: Int) = DB.withSession { implicit session =>
    all.filter(_.number === number).firstOption
  }
  def findByName(name: String) = DB.withSession { implicit session =>
    all.filter(_.name === name).firstOption
  }

  implicit object ReadWrite extends Writes[DraftState] {
    def writes(o: DraftState) = {
      toJson(Map(
        "name" -> toJson(o.name)
      ))
    }
  }
}