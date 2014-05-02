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
  losses: Option[Int] = None,
  sos: Option[Double] = None
){
  def validate = this match {
    case Record(_, None, None, _) => this
    case Record(_, Some(2), Some(0), _) => this
    case Record(_, Some(2), Some(1), _) => this
    case Record(_, Some(1), Some(2), _) => this
    case Record(_, Some(0), Some(2), _) => this
    case _ => throw InvalidRecord()
  }
  def setSos(newSos: Option[Double]) = copy(sos = newSos)
  def getScore(score: Option[Int])(implicit safe: Boolean) = score match {
    case Some(score) => score
    case None => if(safe){ throw IncompleteRecords() }else{ 0 }
  }
  def getWins(implicit safe: Boolean) = getScore(wins)
  def getLosses(implicit safe: Boolean) = getScore(losses)
  def aggregate(next: Record)(implicit safe: Boolean) = Record(
    player,
    Some(getWins + next.getWins),
    Some(getLosses + next.getLosses)
  )
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
    def getRound(draft: Draft, round: Int) = DB.withSession { implicit session =>
      all
        .filter(_.draftHash === draft.hash)
        .filter(_.round === round)
        .list.toSet map { raw:MatchRaw => raw.process }
    }
    def perRoundStandings(draft: Draft) = DB.withTransaction { implicit session =>
      all
        .filter(_.draftHash === draft.hash)
        .list
        .map { case raw:MatchRaw => raw.process }
        .groupBy(_.round)
        .mapValues(
          matches => matches.foldRight(List[Record]())(
            (vs, recs) => recs ++ vs.results
          )
        )
    }
    def totalStandings(draft: Draft)(implicit safe: Boolean) = DB.withTransaction { implicit session =>
      // Combine matches for the same player, then sum the records
      val winLosses = all
        .filter(_.draftHash === draft.hash)
        .list
        .map { case raw:MatchRaw => raw.process }
        .foldRight(List[Record]())(
          (vs, recs) => recs ++ vs.results
        )
        .groupBy(_.player)
        .map { case (player, records) =>
          records.foldRight(Record(player, Some(0), Some(0))) {
            _.aggregate(_)
          }
        }.toList
      // Calculate SOS and return sorted standings
      strengthOfSchedule(
        draft, winLosses
      ).toSeq.sortBy(
        player => (-player.wins.get, player.losses.get, -player.sos.get)
      ).toList
    }

    def strengthOfSchedule(
      draft: Draft, standings: Iterable[Record]
    )(implicit safe: Boolean) = DB.withTransaction { implicit session =>
      // Opponents of each player in standings
      val oppsList = standings map {
        case Record(player, _, _, _) => opponents(draft, player)
      }
      // Opponents of opponents of each player in standings
      val oppsOppsList = (standings zip oppsList) map {
        case (standing, opps) => opps flatMap { opponents(draft, _, standing.player) }
      }
      // Sum opponents' records
      val oppsRecs = (standings zip oppsList) map {
        case (standing, opps) => standings
          .filter(record => opps contains record.player)
          .foldLeft(Record(standing.player, Some(0), Some(0))) {
            _.aggregate(_)
          }
      }
      // Sum opponents' opponents' records
      val oppsOppsRecs = (standings zip oppsOppsList) map {
        case (standing, opps) => standings
          .filter(record => opps contains record.player)
          .foldLeft(Record(standing.player, Some(0), Some(0))) {
            _.aggregate(_)
          }
      }
      // Return original standings with calculated SOS
      (standings, oppsRecs, oppsOppsRecs).zipped map {
        case (standing, oppsRec, oppsOppsRec) => {
          val orTotal = oppsRec.getWins+oppsRec.getLosses
          val orNorm = (try {
            oppsRec.getWins/orTotal
          } catch { case e:ArithmeticException => 0.0 })
          val orrTotal = oppsOppsRec.getWins+oppsOppsRec.getLosses
          val orrNorm = (try {
            oppsOppsRec.getWins/orrTotal
          } catch { case e:ArithmeticException => 0.0 })
          standing.setSos(Some((2.0*(orNorm)+orrNorm)/3.0))
        }
      }
    }
    def opponents(
      draft :Draft, player: Long, exclude: Long = -1
    ) = DB.withSession { implicit session =>
      (all
        .filter(_.draftHash === draft.hash)
        .filter(_.player1 === player)
        .map(_.player2)++
      all
        .filter(_.draftHash === draft.hash)
        .filter(_.player2 === player)
        .map(_.player1))
      .list
      .filter(
        opponent => opponent != exclude
      )
    }

    def replaceCurrentRound(
      draft: Draft, matches: Set[Match]
    ) = DB.withTransaction { implicit session =>
      val expectedRound = removeCurrentRound(draft)+1
      matches map { roundMatch =>
        assert(roundMatch.draftHash == draft.hash)
        assert(roundMatch.round == expectedRound)
        add(roundMatch)
        roundMatch
      }
    }
    def makeNewRound(draft: Draft) = DB.withTransaction { implicit session =>
      rounds(draft) match {
        case 0 => makeFirstRound(draft: Draft)
        case round => makeNextRound(draft: Draft, round)
      }
    }
    def makeFirstRound(draft: Draft) = DB.withTransaction { implicit session =>
      val players = Random.shuffle(Participant.Data.forDraft(draft))
      players.grouped(2).toList map {
        pair => add(Match(draft.hash, Set(
            Record(pair(0).userId),
            Record(pair(1).userId)
          ), 1))
      }
      getCurrentRound(draft)
    }
    def makeNextRound(draft: Draft, round: Int) = DB.withTransaction { implicit session =>
      val players = totalStandings(draft)(true)
      players.grouped(2).toList map {
        pair => add(Match(draft.hash, Set(
            Record(pair(0).player),
            Record(pair(1).player)
          ), round+1))
      }
      getCurrentRound(draft)
    }

    def getCurrentRound(draft: Draft) = DB.withTransaction { implicit session =>
      getRound(draft, rounds(draft))
    }
    def getAllRounds(draft: Draft) = DB.withTransaction { implicit session =>
      1 to rounds(draft) map { getRound(draft, _) }
    }
  }

  def rounds(hash :String)(user: User) = DB.withTransaction { implicit session =>
    Data.rounds(Draft.Data.findByHash(hash)(user))
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

  def makeNewRound(
    hash: String, matches: Set[Match]
  )(user:User) = DB.withTransaction { implicit session =>
    val draft = Draft.Data.findByHash(hash)(user)
    Data.replaceCurrentRound(draft, matches)
    Data.makeNewRound(draft)
  }

  def standings(hash: String)(user:User) = DB.withTransaction { implicit session =>
    val draft = Draft.Data.findByHash(hash)(user)
    (
      Data.perRoundStandings(draft),
      Data.totalStandings(draft)(false)
    )
  }

  implicit object RecordRWFormat extends Format[Record] {
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

  implicit object RWFormat extends Format[Match] {
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