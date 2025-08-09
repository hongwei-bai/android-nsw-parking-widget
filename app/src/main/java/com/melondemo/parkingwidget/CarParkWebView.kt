package com.melondemo.parkingwidget

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
class LoggingWebView(context: Context) : WebView(context) {
    init {
        settings.javaScriptEnabled = true
        webViewClient = WebViewClient()
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        Log.d("bbbb", "ScrollX: $l, ScrollY: $t")
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CarParkWebView() {
    val url = "https://transportnsw.info/travel-info/ways-to-get-around/drive/parking/transport-parkride-car-parks"
    val initScrollX = 0
    val initScrollY = 8069
    val initScrollInnerX = 0
    val initScrollInnerY = 555
    var webView: WebView? by remember { mutableStateOf(null) }
    val cssSelector = "div.container.overflow-y-auto"

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true

                // Set WebChromeClient to catch console.log from JS
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                        val msg = consoleMessage.message()
                        if (msg.startsWith("INNER_SCROLL:")) {
                            val parts = msg.removePrefix("INNER_SCROLL:").split(",")
                            val innerX = parts.getOrNull(0)?.toIntOrNull()
                            val innerY = parts.getOrNull(1)?.toIntOrNull()
                            Log.d("InnerScroll", "x=$innerX, y=$innerY")
                        }
                        if (msg == "Scroll listener attached and scroll preset") {
                            Log.d("InnerScroll", msg)
                        }
                        return true
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)

                        // JS: wait for scrollable element, set initial scroll, attach listener
                        val js = """
                        (function waitForElement() {
                            const el = document.querySelector("$cssSelector");
                            if (!el) {
                                setTimeout(waitForElement, 500);
                                return;
                            }
                            el.scrollTo($initScrollInnerX, $initScrollInnerY);
                            el.addEventListener('scroll', () => {
                                console.log('INNER_SCROLL:' + el.scrollLeft + ',' + el.scrollTop);
                            });
                            console.log("Scroll listener attached and scroll preset");
                        })();
                        """.trimIndent()

                        view.evaluateJavascript(js, null)
                    }
                }

                loadUrl(url)

                // Save reference for scrolling later (optional)
                webView = this
            }
        },
        update = { view ->
            // Scroll the whole page (outer WebView) to initial position after load
            view.postDelayed({
                view.scrollTo(initScrollX, initScrollY)
            }, 500)
        }
    )
}
