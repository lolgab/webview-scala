package example

import com.raquo.laminar.api.L._
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.Thenable.Implicits._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.concurrent.Future
import autowire._

object Main extends App {
  val api = webview.AutowireClient[Api]
  val inputField = Var("")
  val returns = Var(Seq.empty[Greeting])
  val app = div(
    input(
      placeholder := "Insert your name",
      controlled(
        value <-- inputField.signal,
        onInput.mapToValue --> inputField.writer
      )
    ),
    button(
      onClick.mapTo(inputField.signal.now()) --> { name =>
        api.hello(name).call().foreach { res =>
          returns.writer.onNext(returns.now() :+ res)
        }
      },
      "Click me!"
    ),
    ul(
      children <-- returns.signal.map(r => r.map(g => li(g.greet)))
    )
  )
  render(dom.document.getElementById("app"), app)
}
