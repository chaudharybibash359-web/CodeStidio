package com.example.runner

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.example.data.ProjectFileEntity

data class ConsoleLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val level: String, // "LOG", "WARN", "ERROR", "INFO"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

class SandboxBridge(
    private val onLogReceived: (ConsoleLog) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun postConsole(level: String, message: String) {
        val safeMsg = message.take(500)
        mainHandler.post {
            onLogReceived(ConsoleLog(level = level, message = safeMsg))
        }
    }

    @JavascriptInterface
    fun postError(message: String) {
        val safeMsg = message.take(500)
        mainHandler.post {
            onLogReceived(ConsoleLog(level = "ERROR", message = safeMsg))
        }
    }

    companion object {
        fun buildStandaloneHtml(files: List<ProjectFileEntity>): String {
            val htmlFile = files.find { it.fileType == "HTML" || it.name.endsWith(".html") }
                ?: files.firstOrNull()

            var htmlContent = htmlFile?.content ?: "<html><body><h1>No HTML file found</h1></body></html>"

            // Gather all CSS files
            val cssFiles = files.filter { it.fileType == "CSS" || it.name.endsWith(".css") }
            val inlineCss = cssFiles.joinToString("\n") { "/* ${it.name} */\n" + it.content }

            // Gather all JS files
            val jsFiles = files.filter { it.fileType == "JS" || it.name.endsWith(".js") }
            val inlineJs = jsFiles.joinToString("\n") { "// ${it.name}\n" + it.content }

            // Inject Bridge Script with throttling to prevent IPC buffer overflows
            val bridgeScript = """
<script>
(function() {
  const origLog = console.log;
  const origWarn = console.warn;
  const origError = console.error;
  const origInfo = console.info;

  let lastLogTime = 0;
  let logCount = 0;

  function stringify(arg) {
    try {
      if (typeof arg === 'object' && arg !== null) return JSON.stringify(arg);
      return String(arg);
    } catch(e) { return String(arg); }
  }

  console.log = function(...args) {
    origLog.apply(console, args);
    const now = Date.now();
    if (now - lastLogTime > 150 || logCount < 10) {
      logCount++;
      lastLogTime = now;
      if (window.AndroidBridge) {
        try {
          window.AndroidBridge.postConsole('LOG', args.map(stringify).join(' '));
        } catch(e) {}
      }
    }
  };
  console.warn = function(...args) {
    origWarn.apply(console, args);
    if (window.AndroidBridge) {
      try {
        window.AndroidBridge.postConsole('WARN', args.map(stringify).join(' '));
      } catch(e) {}
    }
  };
  console.error = function(...args) {
    origError.apply(console, args);
    if (window.AndroidBridge) {
      try {
        window.AndroidBridge.postConsole('ERROR', args.map(stringify).join(' '));
      } catch(e) {}
    }
  };
  console.info = function(...args) {
    origInfo.apply(console, args);
    if (window.AndroidBridge) {
      try {
        window.AndroidBridge.postConsole('INFO', args.map(stringify).join(' '));
      } catch(e) {}
    }
  };

  window.addEventListener('error', function(e) {
    if (window.AndroidBridge) {
      try {
        window.AndroidBridge.postError((e.message || 'Script error') + ' at line ' + (e.lineno || 0));
      } catch(err) {}
    }
  });
})();
</script>
""".trimIndent()

            // Strip local external stylesheet links and script tags
            htmlContent = htmlContent.replace(Regex("<link[^>]+rel=[\"']stylesheet[\"'][^>]*>", RegexOption.IGNORE_CASE), "")
            htmlContent = htmlContent.replace(Regex("<script[^>]+src=[\"'](?!(http|https|//))[^\"']+[\"'][^>]*>\\s*</script>", RegexOption.IGNORE_CASE), "")

            // Replace external <link rel="stylesheet"> or append inline
            if (inlineCss.isNotBlank()) {
                val styleTag = "<style>\n$inlineCss\n</style>"
                htmlContent = if (htmlContent.contains("</head>", ignoreCase = true)) {
                    htmlContent.replace("</head>", "$styleTag\n</head>")
                } else {
                    styleTag + htmlContent
                }
            }

            // Inject bridge script at top of head or body
            htmlContent = if (htmlContent.contains("<head>", ignoreCase = true)) {
                htmlContent.replace("<head>", "<head>\n$bridgeScript")
            } else if (htmlContent.contains("<html>", ignoreCase = true)) {
                htmlContent.replace("<html>", "<html>\n<head>$bridgeScript</head>")
            } else {
                bridgeScript + htmlContent
            }

            // Inject inline scripts before </body> or append
            if (inlineJs.isNotBlank()) {
                val scriptTag = "<script>\n$inlineJs\n</script>"
                htmlContent = if (htmlContent.contains("</body>", ignoreCase = true)) {
                    htmlContent.replace("</body>", "$scriptTag\n</body>")
                } else {
                    htmlContent + "\n" + scriptTag
                }
            }

            return htmlContent
        }

        fun dispatchKey(webView: WebView?, key: String, isDown: Boolean) {
            if (webView == null) return
            val eventType = if (isDown) "keydown" else "keyup"
            val js = """
                (function() {
                  try {
                    const ev = new KeyboardEvent('$eventType', {
                      key: '$key',
                      code: '$key',
                      bubbles: true,
                      cancelable: true
                    });
                    window.dispatchEvent(ev);
                    document.dispatchEvent(ev);
                  } catch(e) {}
                })();
            """.trimIndent()
            webView.post {
                try {
                    webView.evaluateJavascript(js, null)
                } catch (e: Exception) {}
            }
        }
    }
}
