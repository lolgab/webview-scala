package example

import autowire._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalanative.unsafe._
import scala.scalanative.libc.stdio
import webview.internal.CApi._
import webview._

import scala.concurrent.Future

object ApiImpl extends Api {
  def hello(name: String): Future[Greeting] = Future.successful(Greeting(s"Hello $name!"))
}

object Main extends App {
  // read from a file but it can be put in resources as well
  val js = os.read(os.pwd / "out" / "example" / "frontend" / "fastOpt.dest" / "out.js")
  val html = s"<html><head></head><body><div id=\"app\"><script>${js}</script></body></html>"
  val router = webview.AutowireServer.route[Api](ApiImpl)
  val webView =
    WebView()
      .withTitle("My Window")
      .withHtml(html)
      .withRouter(router)

  webView.run()
}
