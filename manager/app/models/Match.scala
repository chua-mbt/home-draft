package manager.models

import common.models._
import manager.exceptions._

import scala.util.Random

import play.api.libs.json._
import play.api.libs.json.Json._
import play.api.Play.current
import play.api.db.slick.DB

import scala.slick.driver.PostgresDriver.simple._

case class Record(
  player: Long,
  wins: Option[Int] = None,
  losses: Option[Int] = None
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
  def draftHashFK = foreignKey("matches_draft_hash_fkey", draftHash, Draft.Data.all)(_.hash)
  def player1FK = foreignKey("participants_player1_fkey", player1, User.all)(_.id)
  def player2FK = foreignKey("participants_player2_fkey", player2, User.all)(_.id)
}

object Match extends HomeDraftModel {
  private[models] object Data {
    lazy val all = TableQuery[MatchTable]

    def add(newMatch: Match) = DB.withTransaction { implicit session =>
      val raw = newMatch.validate.raw
      if(!all
          .filter(_.draftHash === raw.draftHash)
          .filter(_.player1 === raw.player1)
          .filter(_.player2 === raw.player2)
          .filter(_.round === raw.round)
          .exists.run){
        all += raw
      }
      newMatch.draftHash
    }
    def rounds(draft: Draft) = DB.withSession { implicit session =>
      all.filter(_.draftHash === draft.hash).map(_.round).max.run.getOrElse(0)
    }
    def removeAllRounds(draft: Draft) = DB.withSession { implicit session =>
      all.filter(_.draftHash === draft.hash).delete
      rounds(draft)
    }
    def removeCurrentRound(draft: Draft) = DB.withSession { implicit session =>
      all.filter(_.draftHash === draft.hash).filter(_.round === rounds(draft)).delete
      rounds(draft)
    }
    def replaceCurrentRound(
      draft: Draft, matches: Set[Match]
    ) = DB.withTransaction { implicit session =>
      val expectedRound = removeCurrentRound(draft)+1
      matches foreach { roundMatch =>
        assert(roundMatch.draftHash == draft.hash)
        assert(roundMatch.round == expectedRound)
        add(roundMatch)
      }
      expectedRound
    }
    def makeNextRound(draft: Draft) = DB.withTransaction { implicit session =>
      rounds(draft) match {
        case 0 => makeFirstRound(draft: Draft)
        case _ => None//makeNewRound(draft: Draft, _)
      }
    }
    def makeFirstRound(draft: Draft) = DB.withTransaction { implicit session =>
      val participants = Random.shuffle(Participant.Data.forDraft(draft))
      val round = 1
      val matches = participants.grouped(2).toList map {
        pair => add(Match(draft.hash, Set(
            Record(pair(0).userId),
            Record(pair(1).userId)
          ), round))
      }
      round
    }

    def getRound(draft: Draft, round: Int) = DB.withSession { implicit session =>
      all
        .filter(_.draftHash === draft.hash)
        .filter(_.round === round)
        .list.toSet map { raw:MatchRaw => raw.process }
    }
    def getCurrentRound(draft: Draft) = DB.withTransaction { implicit session =>
      getRound(draft, rounds(draft))
    }
    def getAllRounds(draft: Draft) = DB.withTransaction { implicit session =>
      1 to rounds(draft) map { getRound(draft, _) }
    }
  }

  def getCurrentRound(hash :String)(user: User) = DB.withTransaction { implicit session =>
    Data.getCurrentRound(Draft.Data.findByHash(hash)(user))
  }

  def forRound(hash :String, round: Int)(user: User) = DB.withTransaction { implicit session =>
    Data.getRound(Draft.Data.findByHash(hash)(user), round)
  }

  def replaceCurrentRound(
    hash: String, matches: Set[Match]
  )(user: User) = DB.withTransaction { implicit session =>
    Data.replaceCurrentRound(Draft.Data.findByHash(hash)(user), matches)
  }

  implicit object RecordFormat extends Format[Record] {
    def reads(j: JsValue) = JsSuccess(Record(
      User.findByHandle((j \ "player").as[String]).id,
      (j \ "wins").as[Option[Int]],
      (j \ "losses").as[Option[Int]]
    ))

    def writes(o: Record) = {
      toJson(Map(
        "player" -> toJson(User.findById(o.player).handle),
        "wins" -> toJson(o.wins),
        "losses" -> toJson(o.losses)
      ))
    }
  }

  implicit object ReadFormat extends Format[Match] {
    def reads(j: JsValue) = JsSuccess(Match(
      (j \ "draft").as[String],
      (j \ "results").as[Set[Record]],
      (j \ "round").as[Int]
    ).validate)

    def writes(o: Match) = {
      toJson(Map(
        "draft" -> toJson(o.draftHash),
        "results" -> toJson(o.results),
        "round" -> toJson(o.round)
      ))
    }
  }
}