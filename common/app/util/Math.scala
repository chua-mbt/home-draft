package common.util

object Math {
  def isEven( v:Number ) : Boolean = v.longValue % 2 == 0
  def isOdd( v:Number ) : Boolean = v.longValue % 2 != 0
}