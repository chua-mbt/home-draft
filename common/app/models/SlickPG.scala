package common.models

import play.api.Play
import scala.slick.driver.PostgresDriver.simple._

trait SlickPGModel{
  lazy val url = Play.current.configuration.getString("db.default.url").get
  lazy val driver = Play.current.configuration.getString("db.default.driver").get
  lazy val user = Play.current.configuration.getString("db.default.user").get
  lazy val password = Play.current.configuration.getString("db.default.password").get
  lazy val dbConn = Database.forURL(
    url = url, driver = driver,
    user = user, password = password
  )
}