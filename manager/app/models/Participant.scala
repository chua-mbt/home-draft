package manager.models

import common.models._
import manager.exceptions._

import java.sql.Timestamp
import java.util.Date
import scala.util.Random

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.Play.current
import play.api.db.slick.DB

import scala.slick.driver.PostgresDriver.simple._

case class Participant(
  draftHash: String,
  userId: Long,
  joined: Timestamp = new Timestamp((new Date()).getTime()),
  paid: Boolean = false,
  seat: Option[Int] = None
){
  def setSeat(newSeat: Int):Participant = copy(seat = Some(newSeat))
}

class ParticipantTable(tag: Tag) extends Table[Participant](tag, "participants") {
  def draftHash = column[String]("draft_hash", O.NotNull)
  def userId = column[Long]("user_id", O.NotNull)
  def joined = column[Timestamp]("part_joined", O.NotNull, O.Default(
    new Timestamp((new Date()).getTime())
  ))
  def paid = column[Boolean]("part_paid", O.NotNull, O.Default(false))
  def seat = column[Option[Int]]("part_seat")
  def * = (draftHash, userId, joined, paid, seat) <>
    ((Participant.apply _).tupled, Participant.unapply)
  def pk = primaryKey("participants_pkey", (draftHash, userId))
  def draftHashFK = foreignKey("participants_draft_hash_fkey", draftHash, Draft.Data.all)(_.hash)
  def userIdFK = foreignKey("participants_user_id_fkey", userId, User.all)(_.id)
}

object Participant {
  lazy val minimumNumber = 4

  private[models] object Data {
    lazy val all = TableQuery[ParticipantTable]
    def forDraft(draft: Draft) = DB.withTransaction { implicit session =>
      (for {
        participant <- all if (participant.draftHash === draft.hash)
      } yield participant).sortBy(_.joined.asc).list
    }

    def count(hash: String) = DB.withSession { implicit session =>
      all.filter(_.draftHash === hash).list.length
    }

    def add(
      hash: String, handle: String
    )(user: User) = DB.withTransaction { implicit session =>
      Draft.findByHash(hash)(user)
      if (count(hash) >= 8){ throw DraftFull() }
      User.findByHandle(handle) match {
        case User(id, _, _, _, _) => {
          val newParticipant = Participant(hash, id)
          if(all
              .filter(_.draftHash === newParticipant.draftHash)
              .filter(_.userId === newParticipant.userId)
              .exists.run){
            throw UserAlreadyJoined()
          }
          all += newParticipant
          newParticipant
        }
      }
    }
    def remove(
      hash: String, handle: String
    )(user: User) = DB.withTransaction { implicit session =>
      Draft.findByHash(hash)(user)
      if (count(hash) <= 1){ throw DraftMinSize() }
      User.findByHandle(handle) match {
        case User(id, _, _, _, _) => {
          (all
            .filter(_.draftHash === hash)
            .filter(_.userId === id)
            .delete)
        }
      }
    }
    def edit(participant: Participant) = DB.withSession { implicit session =>
      (all
        .filter(_.draftHash === participant.draftHash)
        .filter(_.userId === participant.userId)
        .update(participant))
    }

    def shuffleSeats(draft: Draft) = DB.withTransaction { implicit session =>
      Random.shuffle(forDraft(draft)).zipWithIndex.foreach {
        case (participant, index) => {
          Data.edit(participant.setSeat(index+1))
        }
      }
    }
  }

  def add(hash: String, handle: String)(user: User) = Data.add(hash, handle)(user)
  def remove(hash: String, handle: String)(user: User) = Data.remove(hash, handle)(user)
  def edit(participant: Participant) = Data.edit(participant)

  def forDraft(hash: String)(user: User) = DB.withTransaction { implicit session =>
    Data.forDraft(Draft.Data.findByHash(hash)(user))
  }

  implicit object ReadWrite extends Writes[Participant] {
    def writes(o: Participant) = {
      toJson(Map(
        "user" -> toJson(User.findById(o.userId).handle),
        "joined" -> toJson(o.joined),
        "paid" -> toJson(o.paid),
        "seat" -> toJson(o.seat)
      ))
    }
  }
}