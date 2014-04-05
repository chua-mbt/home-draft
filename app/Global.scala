import akka.actor._

import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Akka._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.duration._
import scala.concurrent.Future

import common.models._

object Global extends GlobalSettings {
  lazy val setSrc = "https://api.deckbrew.com/mtg/sets"
  var setUpdater: Option[Cancellable] = None

  override def onStart(app: play.api.Application){
    setUpdater = Some(system.scheduler.schedule(0 milliseconds, 24 hour) {
      WS.url(setSrc).get().map {
        response => response.json.as[JsArray].value.map {
          set => MTGSet.add(MTGSet(
            (set \ "id").as[String],
            (set \ "name").as[String]
          ))
        }
      }
    })
  }

  override def onStop(app: play.api.Application){
    setUpdater.map(_.cancel())
  }

  override def onHandlerNotFound(request: RequestHeader) = {
    Future.successful(NotFound(views.html.error(
      "Error 404: Not Found",
      "We can't find what you're looking for."
    )))
  }
}
