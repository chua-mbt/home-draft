package manager.models

import common.models._

import java.sql.Timestamp
import java.security.MessageDigest
import java.security.SecureRandom

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.Play.current
import play.api.db.slick.DB

import scala.slick.driver.PostgresDriver.simple._

case class Draft(
  var hash: String,
  start: Timestamp,
  set1: String,
  set2: String,
  set3: String,
  state: Int,
  venue: Option[String] = None,
  food: Option[String] = None,
  fee: Option[Float] = None,
  details: Option[String] = None
)

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
  def stateFK = foreignKey("drafts_draft_state_fkey", state, DraftState.all)(_.number)
  def set1FK = foreignKey("drafts_draft_set1_fkey", set1, MTGSet.all)(_.id)
  def set2FK = foreignKey("drafts_draft_set2_fkey", set2, MTGSet.all)(_.id)
  def set3FK = foreignKey("drafts_draft_set3_fkey", set3, MTGSet.all)(_.id)
}

object Draft{
  val jodaTSFormat = "yyyy-MM-DD'T'HH:mm"
  lazy val all = TableQuery[DraftTable]
  lazy val allSorted = all.sortBy(_.start.desc)
  def add(newDraft: Draft, userHandle: String) = DB.withSession { implicit session =>
    newDraft.hash = newHash(userHandle)
    if(!all.filter(_.hash === newDraft.hash).exists.run){ all += newDraft }
    newDraft.hash
  }
  def paged(params: PageParam) = DB.withTransaction { implicit session =>
    (allSorted.drop(params.start).take(params.count).list, allSorted.list.length)
  }
  def findByHash(hash: String) = DB.withSession { implicit session =>
    all.filter(_.hash === hash).firstOption
  }
  def newHash(seed:String) = {
    new sun.misc.BASE64Encoder().encode(
      MessageDigest.getInstance("SHA-1").digest(
        (seed + System.currentTimeMillis.toString).getBytes
      )
    ) replace('+', '-') replace('/','_') replace("=", "")
  }

  implicit object ReadWrite extends Writes[Draft] {
    def writes(o: Draft) = {
      toJson(Map(
        "hash" -> toJson(o.hash),
        "start" -> toJson(o.start),
        "set1" -> toJson(o.set1),
        "set2" -> toJson(o.set2),
        "set3" -> toJson(o.set3),
        "venue" -> toJson(o.venue),
        "food" -> toJson(o.food),
        "state" -> toJson(o.state),
        "fee" -> toJson(o.fee),
        "details" -> toJson(o.details)
      ))
    }
  }
}