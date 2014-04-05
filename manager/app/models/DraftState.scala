package manager.models

import common.models._

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
}