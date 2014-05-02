package manager.models

import common.models._
import common.util._
import manager.exceptions._

import java.text.SimpleDateFormat
import java.sql.Timestamp
import java.security.MessageDigest
import java.security.SecureRandom

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.Play.current
import play.api.db.slick.DB

import scala.slick.driver.PostgresDriver.simple._

case class Draft(
  hash: String,
  start: Timestamp,
  set1: String,
  set2: String,
  set3: String,
  state: Int,
  venue: Option[String] = None,
  food: Option[String] = None,
  fee: Option[Float] = None,
  details: Option[String] = None
){
  def setHash(newHash: String):Draft = copy(hash = newHash)
  def setState(newState: Int):Draft = copy(state = newState)
}

class DraftTable(tag: Tag) extends Table[Draft](tag, "drafts") {
  def hash = column[String]("draft_hash", O.PrimaryKey)
  def start = column[Timestamp]("draft_start", O.NotNull)
  def set1 = column[String]("draft_set1", O.NotNull)
  def set2 = column[String]("draft_set2", O.NotNull)
  def set3 = column[String]("draft_set3", O.NotNull)
  def state = column[Int]("draft_state", O.NotNull)
  def venue = column[Option[String]]("draft_venue")
  def food = column[Option[String]]("draft_food")
  def fee = column[Option[Float]]("draft_fee")
  def details = column[Option[String]]("draft_details")
  def * = (hash, start, set1, set2, set3, state, venue, food, fee, details) <>
    ((Draft.apply _).tupled, Draft.unapply)
  def stateFK = foreignKey("drafts_draft_state_fkey", state, DraftState.Data.all)(_.number)
  def set1FK = foreignKey("drafts_draft_set1_fkey", set1, MTGSet.all)(_.id)
  def set2FK = foreignKey("drafts_draft_set2_fkey", set2, MTGSet.all)(_.id)
  def set3FK = foreignKey("drafts_draft_set3_fkey", set3, MTGSet.all)(_.id)
}

object Draft extends HomeDraftModel {
  val tsFormat = "yyyy-MM-dd'T'HH:mm"

  private[models] object Data {
    lazy val all = TableQuery[DraftTable]
    lazy val allSorted = all.sortBy(_.start.desc)

    def newHash(seed: String) = {
      new sun.misc.BASE64Encoder().encode(
        MessageDigest.getInstance("SHA-1").digest(
          (seed + System.currentTimeMillis.toString).getBytes
        )
      ) replace('+', '-') replace('/','_') replace("=", "")
    }
    def findByHash(hash: String)(user: User) = DB.withSession { implicit session =>
      extract(
        (for {
          participant <- Participant.Data.all if participant.userId === user.id
          draft <- all if (
            draft.hash === participant.draftHash && draft.hash === hash
          )
        } yield draft).take(1).firstOption,
        DraftNotFound()
      )
    }
    def isReady(draft: Draft) = DB.withSession { implicit session =>
      val participants = Participant.Data.count(draft)
      (
        (participants >= Participant.minimumNumber) &&
        (Math.isEven(participants))
      )
    }
    def paged(
      params: PageParam, state: Option[String]
    )(user: User) = DB.withTransaction { implicit session =>
      var userDrafts = for {
        participant <- Participant.Data.all if participant.userId === user.id
        draft <- Data.all if draft.hash === participant.draftHash
      } yield draft
      state map {
        name => {
          userDrafts = for{
            dstate <- DraftState.Data.all if dstate.name === name
            draft <- userDrafts if draft.state === dstate.number
          } yield draft
        }
      }
      userDrafts.sortBy(_.start.desc).drop(params.start).take(params.count).list
    }

    def add(newDraft: Draft)(user: User) = DB.withTransaction { implicit session =>
      val draft = newDraft.setHash(newHash(user.handle))
      if(!all.filter(_.hash === draft.hash).exists.run){
        all += draft
        Participant.Data.all += Participant(
          draft.hash, user.id
        )
      }
      draft.hash
    }
    def edit(draft: Draft)(user: User) = DB.withTransaction { implicit session =>
      Data.findByHash(draft.hash)(user)
      all.filter(_.hash === draft.hash).update(draft)
    }

    def changeState(draft: Draft, name: String) = DB.withTransaction { implicit session =>
      val newState = DraftState.Data.findByName(name)
      all.filter(_.hash === draft.hash).update(
        draft.setState(newState.number)
      )
      newState
    }
  }

  def paged(params: PageParam, state: Option[String] = None)(user: User) =
    Data.paged(params, state)(user)

  def add(newDraft: Draft)(user: User) = Data.add(newDraft)(user)
  def edit(draft: Draft)(user: User) = Data.edit(draft)(user)
  def findByHash(hash: String)(user: User) = Data.findByHash(hash)(user)

  def nextState(hash: String)(user: User) = DB.withTransaction { implicit session =>
    DraftState.Data.transitionFor(
      Data.findByHash(hash)(user)
    ).next
  }
  def previousState(hash: String)(user: User) = DB.withTransaction { implicit session =>
    DraftState.Data.transitionFor(
      Data.findByHash(hash)(user)
    ).previous
  }
  def abort(hash: String)(user: User) = DB.withTransaction { implicit session =>
    DraftState.Data.transitionFor(
      Data.findByHash(hash)(user)
    ).abort
  }

  implicit object ReadWrite extends Writes[Draft] {
    def writes(o: Draft) = {
      toJson(Map(
        "hash" -> toJson(o.hash),
        "start" -> toJson(new SimpleDateFormat(tsFormat).format(o.start)),
        "set1" -> toJson(o.set1),
        "set2" -> toJson(o.set2),
        "set3" -> toJson(o.set3),
        "venue" -> toJson(o.venue),
        "food" -> toJson(o.food),
        "state" -> toJson(DraftState.Data.findByNumber(o.state).name),
        "fee" -> toJson(o.fee),
        "details" -> toJson(o.details),
        "participants" -> toJson(Participant.Data.count(o))
      ))
    }
  }
}