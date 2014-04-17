package manager.models

import common.models._

import java.sql.Timestamp
import java.util.Date

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
)

class ParticipantTable(tag: Tag) extends Table[Participant](tag, "participants") {
  def draftHash = column[String]("draft_hash", O.PrimaryKey)
  def userId = column[Long]("user_id", O.NotNull)
  def joined = column[Timestamp]("part_joined", O.NotNull, O.Default(
    new Timestamp((new Date()).getTime())
  ))
  def paid = column[Boolean]("part_paid", O.NotNull, O.Default(false))
  def seat = column[Option[Int]]("part_seat")
  def * = (draftHash, userId, joined, paid, seat) <>
    ((Participant.apply _).tupled, Participant.unapply)
  def pk = primaryKey("participants_pkey", (draftHash, userId))
  def draftHashFK = foreignKey("participants_draft_hash_fkey", draftHash, Draft.all)(_.hash)
  def userIdFK = foreignKey("participants_user_id_fkey", userId, User.all)(_.id)
}

object Participant {
  lazy val all = TableQuery[ParticipantTable]
  def forDraft(hash: String) = DB.withTransaction { implicit session =>
    (for {
      participant <- Participant.all
      draft <- Draft.all if (
        draft.hash === participant.draftHash && draft.hash === hash
      )
    } yield participant).sortBy(_.joined.asc).list
  }
  def count(hash: String) = DB.withSession { implicit session =>
    all.filter(_.draftHash === hash).list.length
  }

  def add(newParticipant: Participant) = DB.withTransaction { implicit session =>
    if(!all
        .filter(_.draftHash === newParticipant.draftHash)
        .filter(_.userId === newParticipant.userId)
        .exists.run &&
      count(newParticipant.draftHash) < 8){
      all += newParticipant
    }
    newParticipant
  }
  def remove(hash: String, userId: Long) = DB.withSession { implicit session =>
    (all
      .filter(_.draftHash === hash)
      .filter(_.userId === userId)
      .delete)
  }

  implicit object ReadWrite extends Writes[Participant] {
    def writes(o: Participant) = {
      toJson(Map(
        "user" -> toJson(User.findById(o.userId).get.handle),
        "joined" -> toJson(o.joined),
        "paid" -> toJson(o.paid),
        "seat" -> toJson(o.seat)
      ))
    }
  }
}