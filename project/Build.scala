import sbt._
import Keys._
import play.Project._
import net.litola.SassPlugin

object ApplicationBuild extends Build {

    val appName         = "home-draft"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      jdbc, anorm, //cache,
      "postgresql" % "postgresql" % "9.1-901.jdbc4",
      "com.typesafe.slick" %% "slick" % "2.0.0",
      "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.typesafe.play" %% "play-slick" % "0.6.0.1"
    )

    lazy val common = play.Project(
      appName + "-common", appVersion, appDependencies, path = file("common")
    ).settings(
      SassPlugin.sassSettings:_*
    )

    lazy val admin = play.Project(
      appName + "-admin", appVersion, appDependencies, path = file("admin")
    ).dependsOn(common)

    lazy val manager = play.Project(
      appName + "-manager", appVersion, appDependencies, path = file("manager")
    ).dependsOn(common)

    val _main = play.Project(
      appName, appVersion, appDependencies
    ).dependsOn(common, manager)
}