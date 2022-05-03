package webview.internal

import scala.scalanative.runtime._
import scala.scalanative.unsafe._

object WebViewUtils {
  def fromPtr[T](ptr: Ptr[Byte]): T = {
    val rawPtr = toRawPtr(ptr)
    Intrinsics.castRawPtrToObject(rawPtr).asInstanceOf[T]
  }
  def toPtr[T <: Object](t: T): Ptr[Byte] = {
    val rawPtr = Intrinsics.castObjectToRawPtr(t)
    fromRawPtr[Byte](rawPtr)
  }
}
