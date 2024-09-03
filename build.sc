import mill.scalajslib.ScalaJSModule
import mill.define.Target
import mill._
import mill.scalalib._
import mill.scalanativelib._

object Common {
  private[Common] trait Common extends ScalaModule {
    def scalaVersion = "2.13.14"
  }
  trait Native extends Common with ScalaNativeModule {
    def scalaNativeVersion = "0.4.17"
  }
  trait JS extends Common with ScalaJSModule {
    def scalaJSVersion = "1.16.0"
  }
}

// object `webview-header` extends Common {
//   override def generatedResources: T[Seq[PathRef]] = T {
//     val content = requests.get.stream("https://raw.githubusercontent.com/webview/webview/master/webview.h")
//     val dest = T.dest / "webview.h"
//     os.write(dest, content)
//     Seq(PathRef(dest))
//   }
// }

trait WebViewApp extends ScalaNativeModule {
  def optionsFromCommand(command: String) = os
    .proc(command.split(' '))
    .call()
    .out
    .text()
    .trim
    .split(' ')
    .toIndexedSeq

  def linuxCompileOptions = optionsFromCommand(
    "pkg-config --cflags gtk+-3.0 webkit2gtk-4.0"
  )
  def linuxLinkingOptions = optionsFromCommand(
    "pkg-config --libs gtk+-3.0 webkit2gtk-4.0"
  )
  def macOptions = Array("-framework", "WebKit")

  sealed trait Platform
  object Platform {
    case object Linux extends Platform
    case object Mac extends Platform
    case object Windows extends Platform
  }

  def compileOptions(platform: Platform) = platform match {
    case Platform.Linux =>
      optionsFromCommand(
        "pkg-config --cflags gtk+-3.0 webkit2gtk-4.0"
      )
    case Platform.Mac => Seq("-framework", "WebKit")

    case Platform.Windows => Seq.empty
  }
  def linkingOptions(platform: Platform) = platform match {
    case Platform.Linux =>
      optionsFromCommand(
        "pkg-config --libs gtk+-3.0 webkit2gtk-4.0"
      )
    case Platform.Mac => Seq("-framework", "WebKit")
    // Not tested on windows yet
    case Platform.Windows => Seq("-mwindows", "-lwebview", "-lWebView2Loader")
  }

  def platform: Platform = {
    val name = System.getProperty("os.name").toLowerCase().replaceAll(" ", "")
    if (name.contains("osx")) Platform.Mac
    else if (name.contains("linux")) Platform.Linux
    else if (name.contains("windows")) Platform.Windows
    else throw new Exception(s"Unrecognised platform $name")
  }

  override def nativeCompileOptions = T {
    super.nativeCompileOptions() ++ compileOptions(platform)
  }
  override def nativeLinkingOptions = T {
    super.nativeLinkingOptions() ++ linkingOptions(platform)
  }
}

val sharedIvyDeps = Agg(
  ivy"com.lihaoyi::upickle::3.2.0",
  ivy"com.lihaoyi::autowire::0.3.3"
)

object webview extends Common.Native {
  def ivyDeps = super.ivyDeps() ++ sharedIvyDeps
}
object `webview-client` extends Common.JS {
  def ivyDeps = super.ivyDeps() ++ sharedIvyDeps
}

object example extends Module {

  object frontend extends Common.JS {
    def moduleDeps = Seq(`webview-client`)
    def ivyDeps = super.ivyDeps() ++ Agg(
      ivy"com.raquo::laminar::17.1.0"
    )
    def sources = T.sources {
      super.sources() ++ Agg(PathRef(millSourcePath / os.up / "shared" / "src"))
    }
  }
  object backend extends Common.Native with WebViewApp {
    def moduleDeps = Seq(webview)
    def ivyDeps = super.ivyDeps() ++ Agg(
      ivy"com.lihaoyi::os-lib::0.9.3"
    )
    def sources = T.sources {
      super.sources() ++ Agg(PathRef(millSourcePath / os.up / "shared" / "src"))
    }
  }
}
