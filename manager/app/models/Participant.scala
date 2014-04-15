package manager.models

import common.models._

import java.sql.Timestamp

import play.api.Play.current
import play.api.db.slick.DB

import scala.slick.driver.PostgresDriver.simple._

case class Participant(
  draftHash: String,
  userId: Long,
  joined: Timestamp,
  paid: Boolean = false,
  seat: Option[Int] = None
)

class ParticipantTable(tag: Tag) extends Table[Participant](tag, "participants") {
  def draftHash = column[String]("draft_hash", O.PrimaryKey)
  def userId = column[Long]("user_id", O.NotNull)
  def joined = column[Timestamp]("part_joined", O.NotNull)
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
  lazy val allSorted = all.sortBy(_.joined.desc)
  def count(hash: String) = DB.withSession { implicit session =>
    all.filter(_.draftHash === hash).list.length
  }
}