package example

import scala.concurrent.Future
import upickle.default._

case class Greeting(greet: String)
object Greeting {
  implicit val rw = macroRW[Greeting]
}

trait Api {
  def hello(name: String): Future[Greeting]
}
