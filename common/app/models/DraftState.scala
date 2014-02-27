package common.models

import scala.slick.driver.PostgresDriver.simple._

case class DraftState(number: Int, name: String)

class DraftStateTable(tag: Tag) extends Table[DraftState](tag, "dstates") {
  def number = column[Int]("dstate_number", O.PrimaryKey)
  def name = column[String]("dstate_name", O.NotNull)
  def * = (number, name) <> (DraftState.tupled, DraftState.unapply)
}

object DraftStates extends SlickPGModel{
  lazy val all = TableQuery[DraftStateTable]
  lazy val allSorted = all.sortBy(_.number.asc)
}