package zingg.scala

import scala.reflect.runtime.universe._

object TypeTags {
  val SeqInteger = typeTag[Seq[Integer]]
  val Integer = typeTag[Integer]
  def Seq[A](implicit tt: TypeTag[A]) = typeTag[Seq[A]]
  val MapString = typeTag[Map[String, String]]
}
