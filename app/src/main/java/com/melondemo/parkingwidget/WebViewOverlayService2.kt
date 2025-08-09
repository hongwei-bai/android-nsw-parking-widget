package com.melondemo.parkingwidget

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.melondemo.parkingwidget.data.ParkingInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WebViewOverlayService2 : Service() {
    val debug = false
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "webview_overlay_service_channel2"
        const val NOTIFICATION_ID = 67892

        fun isOverlayPermissionGranted(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else {
                true
            }
        }
    }

    private lateinit var windowManager: WindowManager
    private var overlayView: android.view.View? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        if (!isOverlayPermissionGranted(this)) {
            Toast.makeText(this, "Overlay permission required", Toast.LENGTH_LONG).show()
            stopSelf()
            return
        }

        serviceScope.launch {
            while (isActive) {
                delay(60_000)  // 60 seconds delay
                overlayView?.findViewById<WebView>(R.id.carpark_webview)?.reload()
            }
        }

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Inflate your XML layout containing the WebView
        overlayView = LayoutInflater.from(this).inflate(R.layout.carpark_webview, null)
        val webView = overlayView!!.findViewById<WebView>(R.id.carpark_webview)

        setupWebView(webView)

        val widthInDp = 400
        val heightInDp = 150
        val scale = resources.displayMetrics.density
        val widthPx = (widthInDp * scale).toInt()
        val heightPx = (heightInDp * scale).toInt()

        var initialX = 0
        var initialY = 6800
        var initialTouchX = 0f
        var initialTouchY = 2000f

        val layoutParams = WindowManager.LayoutParams(
            widthPx,
            heightPx,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 0
        layoutParams.y = 0

        windowManager.addView(overlayView, layoutParams)

        if (!debug) {
            webView.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(overlayView, layoutParams)
                        true
                    }

                    else -> false
                }
            }
        }

        startForegroundServiceNotification()
    }

    private fun setupWebView(webView: WebView) {
        val url =
            "https://transportnsw.info/travel-info/ways-to-get-around/drive/parking/transport-parkride-car-parks"
        val cssSelector = "div.container.overflow-y-auto"
        val initInnerScrollX = 0
        val initInnerScrollY = ParkingInfo.y2
        val initScrollX = 0
        val initScrollY = 3280

        webView.settings.javaScriptEnabled = true
        if (!debug) {
            webView.setOnTouchListener { _, _ -> true }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                if (debug) {
                    val msg = consoleMessage.message()
                    if (msg.startsWith("INNER_SCROLL:")) {
                        val parts = msg.removePrefix("INNER_SCROLL:").split(",")
                        val innerX = parts.getOrNull(0)?.toIntOrNull()
                        val innerY = parts.getOrNull(1)?.toIntOrNull()
                        if (innerX != null && innerY != null) {
                            Log.d("InnerScroll", "  inner x=$innerX, y=$innerY")
                        }
                    } else if (msg.startsWith("ROOT_SCROLL:")) {
                        val parts = msg.removePrefix("ROOT_SCROLL:").split(",")
                        val innerX = parts.getOrNull(0)?.toIntOrNull()
                        val innerY = parts.getOrNull(1)?.toIntOrNull()
                        if (innerX != null && innerY != null) {
                            Log.d("InnerScroll", "x=$innerX, y=$innerY")
                        }
                    }
                }
                return true
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                view.postDelayed({
                    val js = if (debug) {
                        """
                    (function() {
                      const el = document.querySelector("$cssSelector");
                      if (!el) return;
                      window.scrollTo($initScrollX, $initScrollY);
                      el.addEventListener('scroll', () => {
                        console.log('INNER_SCROLL:' + el.scrollLeft + ',' + el.scrollTop);
                      });
                      window.addEventListener('scroll', () => {
                        console.log('ROOT_SCROLL:' + window.scrollX + ',' + window.scrollY);
                      });
                      el.scrollLeft = $initInnerScrollX;
                      el.scrollTop = $initInnerScrollY;
                    })();
                """.trimIndent()
                    } else {
                        """
                    (function() {
                      const el = document.querySelector("$cssSelector");
                      if (!el) return;
                      window.scrollTo($initScrollX, $initScrollY);
                      el.scrollLeft = $initInnerScrollX;
                      el.scrollTop = $initInnerScrollY;
                    })();
                """.trimIndent()
                    }
                    view.evaluateJavascript(js, null)
                }, 300) // delay 300ms or more if needed
                Log.d("bbbb", "onPageFinished")
            }
        }

        webView.loadUrl(url)
    }

    private fun startForegroundServiceNotification() {
        val channelName = "WebView Overlay Service"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(chan)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                PendingIntent.FLAG_MUTABLE else 0
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Parking Overlay Active")
            .setContentText("Tap to open app")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        overlayView?.let { windowManager.removeView(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
