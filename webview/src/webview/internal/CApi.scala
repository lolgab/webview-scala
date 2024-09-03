package webview.internal

import scala.scalanative.unsafe._

@extern
object CApi {
  type webview_t = Ptr[Byte]
  type DispatchCallback = CFuncPtr2[webview_t, Ptr[Byte], Unit]
  type BindCallback = CFuncPtr3[CString, CString, Ptr[Byte], Unit]

  /** Creates a new webview instance. If debug is non-zero - developer tools
    * will be enabled (if the platform supports them). Window parameter can be a
    * pointer to the native window handle. If it's non-null - then child WebView
    * is embedded into the given parent window. Otherwise a new window is
    * created. Depending on the platform, a GtkWindow, NSWindow or HWND pointer
    * can be passed here.
    */
  def webview_create(debug: CInt, window: Ptr[Byte]): webview_t = extern

  /** Destroys a webview and closes the native window.
    */
  def webview_destroy(w: webview_t): Unit = extern

  /** Runs the main loop until it's terminated. After this function exits - you
    * must destroy the webview.
    */
  def webview_run(w: webview_t): Unit = extern

  /** Stops the main loop. It is safe to call this function from another other
    * background thread.
    */
  def webview_terminate(w: webview_t): Unit = extern

  /** Posts a function to be executed on the main thread. You normally do not
    * need to call this function, unless you want to tweak the native window.
    */
  def webview_dispatch(
      w: webview_t,
      fn: DispatchCallback,
      arg: Ptr[Byte]
  ): Unit = extern

  /** Returns a native window handle pointer. When using GTK backend the pointer
    * is GtkWindow pointer, when using Cocoa backend the pointer is NSWindow
    * pointer, when using Win32 backend the pointer is HWND pointer.
    */
  def webview_get_window(w: webview_t): Ptr[Byte] = extern

  /** Updates the title of the native window. Must be called from the UI thread.
    */
  def webview_set_title(w: webview_t, title: CString): Unit = extern

  /** Width and height are default size
    */
  @name("scalanative_WEBVIEW_HINT_NONE")
  var WEBVIEW_HINT_NONE: CInt = extern

  /** Width and height are minimum bounds
    */
  @name("scalanative_WEBVIEW_HINT_MIN")
  var WEBVIEW_HINT_MIN: CInt = extern

  /** Width and height are maximum bounds
    */
  @name("scalanative_WEBVIEW_HINT_MAX")
  var WEBVIEW_HINT_MAX: CInt = extern

  /** Window size can not be changed by a user
    */
  @name("scalanative_WEBVIEW_HINT_FIXED")
  var WEBVIEW_HINT_FIXED: CInt = extern

  /** Updates native window size. See WEBVIEW_HINT constants.ß
    */
  def webview_set_size(
      w: webview_t,
      width: CInt,
      height: CInt,
      hints: CInt
  ): Unit = extern

  /** Updates native window size. See WEBVIEW_HINT constants.ß
    */
  def webview_navigate(
      w: webview_t,
      url: CString,
      height: CInt,
      hints: CInt
  ): Unit = extern

  /** Set webview HTML directly.
    */
  def webview_set_html(w: webview_t, html: CString): Unit = extern

  /** Injects JavaScript code at the initialization of the new page. Every time
    * the webview will open a the new page - this initialization code will be
    * executed. It is guaranteed that code is executed before window.onload.
    */
  def webview_init(w: webview_t, js: CString): Unit = extern

  /** Evaluates arbitrary JavaScript code. Evaluation happens asynchronously,
    * also the result of the expression is ignored. Use RPC bindings if you want
    * to receive notifications about the results of the evaluation.
    */
  def webview_eval(w: webview_t, js: CString): Unit = extern

  /** Binds a native C callback so that it will appear under the given name as a
    * global JavaScript function. Internally it uses webview_init(). Callback
    * receives a request string and a user-provided argument pointer. Request
    * string is a JSON array of all the arguments passed to the JavaScript
    * function.
    */
  def webview_bind(
      w: webview_t,
      name: CString,
      fn: BindCallback,
      arg: Ptr[Byte]
  ): Unit = extern

  /** Removes a native C callback that was previously set by webview_bind. */
  def webview_unbind(w: webview_t, name: CString): Unit = extern

  /** Allows to return a value from the native binding. Original request pointer
    * must be provided to help internal RPC engine match requests with
    * responses. If status is zero - result is expected to be a valid JSON
    * result value. If status is not zero - result is an error JSON object.
    */
  def webview_return(
      w: webview_t,
      seq: CString,
      status: CInt,
      result: CString
  ): Unit = extern
}
