package webview

import scala.scalanative.unsafe._
import webview.internal.CApi._
import webview.internal.WebViewUtils._

class WebView private (
    val title: String,
    val size: WebView.Size,
    val html: String,
    router: AutowireServer.Router
) {
  private def copy[T](
      title: String = this.title,
      size: WebView.Size = this.size,
      html: String = this.html,
      router: AutowireServer.Router = this.router
  ) = new WebView(
    title = title,
    size = size,
    html = html,
    router = router
  )
  def withTitle(title: String): WebView = copy(title = title)
  def withHtml(html: String): WebView = copy(html = html)
  def withRouter(router: AutowireServer.Router): WebView = copy(router = router)

  def run(): Unit = Zone { implicit z =>
    val w: webview_t = webview_create(1, null)
    webview_set_title(w, toCString(title))
    webview_set_size(w, size.width, size.height, size.hint.value)
    val bindArg = WebView.BindArg(webview_t = w, router = router)
    webview_bind(w, c"webviewBridge", WebView.webviewBridge, toPtr(bindArg))
    webview_set_html(
      w,
      toCString(html)
    )
    try {
      webview_run(w)
    } finally {
      webview_destroy(w)
    }
  }
}

object WebView {
  private case class BindArg(
      webview_t: webview_t,
      router: webview.AutowireServer.Router
  )
  import scala.concurrent.ExecutionContext.Implicits.global
  private val webviewBridge: BindCallback =
    (seq: CString, req: CString, arg: Ptr[Byte]) => {
      val bindArg = fromPtr[BindArg](arg)
      val router = bindArg.router
      val reqString = fromCString(req)
      val elems = ujson.read(reqString).arr
      val segments = upickle.default.read[Seq[String]](elems(0))
      val args = upickle.default.read[Map[String, String]](elems(1))
      val resultFuture = bindArg.router(autowire.Core.Request(segments, args))
      resultFuture.foreach { result =>
        Zone { implicit z =>
          webview_return(bindArg.webview_t, seq, 0, toCString(result))
        }
      }
      scala.scalanative.runtime.loop()
    }

  def apply(): WebView = new WebView(
    title = "",
    size = Size.Default,
    html = "",
    router = null
  )
  class Size private (val width: Int, val height: Int, val hint: Size.Hint) {
    private def copy(
        width: Int = this.width,
        height: Int = this.height,
        hint: Size.Hint = this.hint
    ) = new Size(
      width = width,
      height = height,
      hint = hint
    )
  }
  object Size {
    val Default = new Size(width = 480, height = 320, Hint.None)
    sealed abstract class Hint(private[WebView] val value: Int)
    object Hint {
      case object None extends Hint(WEBVIEW_HINT_NONE)
      case object Min extends Hint(WEBVIEW_HINT_MIN)
      case object Max extends Hint(WEBVIEW_HINT_MAX)
      case object Fixed extends Hint(WEBVIEW_HINT_FIXED)
    }
  }
}

object AutowireServer
    extends autowire.Server[
      String,
      upickle.default.Reader,
      upickle.default.Writer
    ] {
  def write[Result: upickle.default.Writer](r: Result) =
    upickle.default.write(r)
  def read[Result: upickle.default.Reader](p: String) =
    upickle.default.read[Result](p)
}
