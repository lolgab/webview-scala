# webview-scala

Scala Native + Scala.js interface for [webview](https://github.com/webview/webview)

## How to run the example

In two separate terminals

Build the frontend first:

```
mill -j 0 -w example.frontend.fastLinkJS
```

Then build and run the application:

```
mill -j 0 -w example.backend.run
```
