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
  hash: String,
  start: Timestamp,
  state: Int,
  venue: Option[String] = None,
  food: Option[String] = None,
  fee: Option[Float] = None,
  set1: Option[String] = None,
  set2: Option[String] = None,
  set3: Option[String] = None
)

class DraftTable(tag: Tag) extends Table[Draft](tag, "drafts") {
  def hash = column[String]("draft_hash", O.PrimaryKey)
  def start = column[Timestamp]("draft_start", O.NotNull)
  def state = column[Int]("draft_state", O.NotNull)
  def venue = column[Option[String]]("draft_venue")
  def food = column[Option[String]]("draft_food")
  def fee = column[Option[Float]]("draft_fee")
  def set1 = column[Option[String]]("draft_set1")
  def set2 = column[Option[String]]("draft_set2")
  def set3 = column[Option[String]]("draft_set3")
  def * = (hash, start, state, venue, food, fee, set1, set2, set3) <>
    ((Draft.apply _).tupled, Draft.unapply)
  def stateFK = foreignKey("drafts_draft_state_fkey", state, DraftState.all)(_.number)
  def set1FK = foreignKey("drafts_draft_set1_fkey", set1, MTGSet.all)(_.id)
  def set2FK = foreignKey("drafts_draft_set2_fkey", set2, MTGSet.all)(_.id)
  def set3FK = foreignKey("drafts_draft_set3_fkey", set3, MTGSet.all)(_.id)
}

object Draft{
  lazy val all = TableQuery[DraftTable]
  lazy val allSorted = all.sortBy(_.start.desc)
  def add(newDraft: Draft) = DB.withSession { implicit session =>
    all += newDraft
  }
  def paged(params: PageParam) = DB.withSession{ implicit session =>
    allSorted.drop(params.start).take(params.count).list
  }
  def findByHash(hash: String) = DB.withSession { implicit session =>
    all.filter(_.hash === hash).firstOption
  }
  def newHash(seed:String) = {
    new sun.misc.BASE64Encoder().encode(
      MessageDigest.getInstance("SHA-1").digest(
        (seed + System.currentTimeMillis.toString).getBytes
      )
    )
  }

  implicit object ReadWrite extends Writes[Draft] {
    def writes(o: Draft) = {
      toJson(Map(
        "hash" -> toJson(o.hash),
        "start" -> toJson(o.start),
        "venue" -> toJson(o.venue),
        "food" -> toJson(o.food),
        "state" -> toJson(o.state),
        "fee" -> toJson(o.fee),
        "set1" -> toJson(o.set1),
        "set2" -> toJson(o.set2),
        "set3" -> toJson(o.set3)
      ))
    }
  }
}