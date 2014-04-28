package manager.models

import common.models._
import manager.exceptions._

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.Play.current
import play.api.db.slick.DB

import scala.slick.driver.PostgresDriver.simple._

case class Record(
  player: Long,
  wins: Option[Int],
  losses: Option[Int]
){
  def validate = this match {
    case Record(_, None, None) => this
    case Record(_, Some(2), Some(0)) => this
    case Record(_, Some(2), Some(1)) => this
    case Record(_, Some(1), Some(2)) => this
    case Record(_, Some(0), Some(2)) => this
    case _ => throw InvalidMatchResults()
  }
}

case class Match(
  draftHash: String,
  results: Set[Record],
  round: Int
){
  def validate = results.foldRight(this) { (rec, ret) => rec.validate;ret }
  def raw = {
    val records = results.toSeq.sortBy(_.player)
    val record1 = records(0)
    val record2 = records(1)
    MatchRaw(
      draftHash,
      record1.player, record1.wins,
      record2.player, record2.wins,
      round
    )
  }
}

case class MatchRaw(
  draftHash: String,
  player1: Long,
  player1Score: Option[Int],
  player2: Long,
  player2Score: Option[Int],
  round: Int
){
  def process = Match(
    draftHash, Set(
      Record(player1, player1Score, player2Score),
      Record(player2, player2Score, player1Score)
    ), round
  )
}

class MatchTable(tag: Tag) extends Table[MatchRaw](tag, "matches") {
  def draftHash = column[String]("draft_hash", O.NotNull)
  def player1 = column[Long]("player1", O.NotNull)
  def player1Score = column[Option[Int]]("player1_score")
  def player2 = column[Long]("player2", O.NotNull)
  def player2Score = column[Option[Int]]("player2_score")
  def round = column[Int]("match_round", O.NotNull)
  def * = (draftHash, player1, player1Score, player2, player2Score, round) <>
    ((MatchRaw.apply _).tupled, MatchRaw.unapply)
  def pk = primaryKey("matches_pkey", (draftHash, player1, player2, round))
  def draftHashFK = foreignKey("matches_draft_hash_fkey", draftHash, Draft.all)(_.hash)
  def player1FK = foreignKey("participants_player1_fkey", player1, User.all)(_.id)
  def player2FK = foreignKey("participants_player2_fkey", player2, User.all)(_.id)
}

object Match extends HomeDraftModel {
  lazy val all = TableQuery[MatchTable]

  private def rounds(draft: Draft) = DB.withSession { implicit session =>
    // create randomized matches, add each
  }
  private def initialize(draft: Draft) = DB.withSession { implicit session =>
    // create randomized matches, add each
  }

  implicit object RecordReadWrite extends Writes[Record] {
    def writes(o: Record) = {
      toJson(Map(
        "player" -> toJson(User.findById(o.player).handle),
        "wins" -> toJson(o.wins),
        "losses" -> toJson(o.losses)
      ))
    }
  }

  implicit object ReadWrite extends Writes[Match] {
    def writes(o: Match) = {
      toJson(Map(
        "draftHash" -> toJson(o.draftHash),
        "results" -> toJson(o.results),
        "round" -> toJson(o.round)
      ))
    }
  }
}