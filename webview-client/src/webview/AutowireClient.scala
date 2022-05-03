package webview

import autowire._
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

object AutowireClient
    extends autowire.Client[
      js.Any,
      upickle.default.Reader,
      upickle.default.Writer
    ] {
  def write[Result: upickle.default.Writer](r: Result) =
    upickle.default.write(r)
  def read[Result: upickle.default.Reader](p: js.Any) =
    upickle.default.read[Result](js.JSON.stringify(p))

  override def doCall(req: Request) = {
    val promise = js.Dynamic.global
      .webviewBridge(req.path.toJSArray, req.args.toJSDictionary)
      .asInstanceOf[js.Promise[js.Any]]
    import scala.concurrent.ExecutionContext.Implicits.global
    promise.toFuture
  }
}
