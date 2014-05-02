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
    if(!Draft.Data.isReady(draft)){ throw DraftNotReady() }
    Participant.Data.shuffleSeats(draft)
    Draft.Data.changeState(draft, "drafting")
  }
  override def previous = { DraftState.Data.findByName("upcoming") }
  override def abort = DB.withTransaction { implicit session =>
    Draft.Data.changeState(draft, "aborted")
  }
}
case class DraftingTrans(draft: Draft) extends Transitions(draft) {
  override def next = DB.withTransaction { implicit session =>
    Match.Data.makeFirstRound(draft)
    Draft.Data.changeState(draft, "tournament")
  }
  override def previous = {
    Match.Data.removeAllRounds(draft)
    Draft.Data.changeState(draft, "upcoming")
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
  private[models] object Data {
    lazy val all = TableQuery[DraftStateTable]
    lazy val allSorted = all.sortBy(_.number.asc)

    def list = DB.withSession { implicit session =>
      Data.allSorted.list
    }

    def transitionFor(draft: Draft) = DraftState.Data.findByNumber(draft.state) match {
      case DraftState(_, "upcoming") => UpcomingTrans(draft)
      case DraftState(_, "drafting") => DraftingTrans(draft)
      case DraftState(_, "aborted") => AbortedTrans(draft)
    }
    def findByNumber(number: Int) = DB.withSession { implicit session =>
      extract(all.filter(_.number === number).take(1).firstOption, DStateNotFound())
    }
    def findByName(name: String) = DB.withSession { implicit session =>
      extract(all.filter(_.name === name).take(1).firstOption, DStateNotFound())
    }
  }

  def findByNumber(number: Int) = Data.findByNumber(number)
  def findByName(name: String) = Data.findByName(name)
  def list = Data.list

  implicit object ReadWrite extends Writes[DraftState] {
    def writes(o: DraftState) = {
      toJson(Map(
        "number" -> toJson(o.number),
        "name" -> toJson(o.name)
      ))
    }
  }
}