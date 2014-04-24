package manager.models

import common.models._
import manager.exceptions._

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.Play.current
import play.api.db.slick.DB

import scala.slick.driver.PostgresDriver.simple._

case class DraftState(number: Int, name: String)

sealed abstract class Transitions(draft: Draft) {
  def next: DraftState; def previous: DraftState
  def abort: DraftState = { throw DraftLocked() }
}
case class UpcomingTrans(draft: Draft) extends Transitions(draft) {
  override def next = DB.withTransaction { implicit session =>
    if(Draft.isReady(draft.hash)){ throw DraftNotReady() }
    Participant.shuffleSeats(draft)
    Draft.changeState(draft, "drafting")
  }
  override def previous = { DraftState.findByName("upcoming") }
  override def abort = DB.withTransaction { implicit session =>
    Draft.changeState(draft, "aborted")
  }
}
case class AbortedTrans(draft: Draft) extends Transitions(draft) {
  override def next = { throw DraftLocked() }
  override def previous = { throw DraftLocked() }
}

class DraftStateTable(tag: Tag) extends Table[DraftState](tag, "dstates") {
  def number = column[Int]("dstate_number", O.PrimaryKey)
  def name = column[String]("dstate_name", O.NotNull)
  def * = (number, name) <> ((DraftState.apply _).tupled, DraftState.unapply)
}

object DraftState extends HomeDraftModel {
  lazy val all = TableQuery[DraftStateTable]
  lazy val allSorted = all.sortBy(_.number.asc)
  def list = DB.withSession { implicit session =>
    allSorted.list
  }
  def findByNumber(number: Int) = DB.withSession { implicit session =>
    extract(all.filter(_.number === number).firstOption, DStateNotFound())
  }
  def findByName(name: String) = DB.withSession { implicit session =>
    extract(all.filter(_.name === name).firstOption, DStateNotFound())
  }
  def transitionFor(draft: Draft) = DraftState.findByNumber(draft.state) match {
    case DraftState(_, "upcoming") => UpcomingTrans(draft)
    case DraftState(_, "aborted") => AbortedTrans(draft)
  }

  implicit object ReadWrite extends Writes[DraftState] {
    def writes(o: DraftState) = {
      toJson(Map(
        "name" -> toJson(o.name)
      ))
    }
  }
}