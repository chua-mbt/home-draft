package common.models

import common.exceptions._

trait HomeDraftModel {
  def extract[T](
    resultOpt: Option[T], exception: RuntimeException
  ) = resultOpt match {
    case Some(result) => result
    case None => throw exception
  }
}