package common.models

import java.sql.Timestamp
import scala.slick.driver.PostgresDriver.simple._

case class Draft(
  id: Long,
  hash: String,
  start: Timestamp,
  venue: String,
  food: String,
  state: Int,
  fee: Float,
  set1: String,
  set2: String,
  set3: String
)

class DraftTable(tag: Tag) extends Table[Draft](tag, "drafts") {
  def id = column[Long]("draft_id", O.PrimaryKey, O.AutoInc)
  def hash = column[String]("draft_hash", O.NotNull)
  def start = column[Timestamp]("draft_start", O.NotNull)
  def venue = column[String]("draft_venue", O.NotNull)
  def food = column[String]("draft_food", O.NotNull)
  def state = column[Int]("draft_state", O.NotNull)
  def fee = column[Float]("draft_state", O.NotNull)
  def set1 = column[String]("draft_set1")
  def set2 = column[String]("draft_set2")
  def set3 = column[String]("draft_set3")
  def * = (id, hash, start, venue, food, state, fee, set1, set2, set3) <>
    (Draft.tupled, Draft.unapply)
  def stateFK = foreignKey("drafts_draft_state_fkey", state, DraftStates.all)(_.number)
  def set1FK = foreignKey("drafts_draft_set1_fkey", set1, MTGSets.all)(_.id)
  def set2FK = foreignKey("drafts_draft_set2_fkey", set2, MTGSets.all)(_.id)
  def set3FK = foreignKey("drafts_draft_set3_fkey", set3, MTGSets.all)(_.id)
}

object Drafts extends SlickPGModel{
  lazy val all = TableQuery[DraftTable]
  lazy val allSorted = all.sortBy(_.start.desc)
  def add(newDraft: Draft) = dbConn withSession { implicit session =>
    all += newDraft
  }
}