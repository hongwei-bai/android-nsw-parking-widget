package com.melondemo.parkingwidget

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.melondemo.parkingwidget.data.CarParkRepository
import com.melondemo.parkingwidget.data.ParkingInfo
import com.melondemo.parkingwidget.ui.theme.AndroidnswparkingwidgetwizardTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    // Launcher to handle returning from permission settings
    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Overlay permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Overlay permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        }

        setContent {
            AndroidnswparkingwidgetwizardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    OverlayControls(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                    )
//                    CarParkWebView()
                }
            }
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            "package:$packageName".toUri()
        )
        overlayPermissionLauncher.launch(intent)
    }

    private fun startOverlayService(serviceId: Int) {
        val service = when (serviceId) {
            1 -> WebViewOverlayService1::class.java
            else -> WebViewOverlayService2::class.java
        }
        val intent = Intent(this, service)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopOverlayService(serviceId: Int) {
        val service = when (serviceId) {
            1 -> WebViewOverlayService1::class.java
            else -> WebViewOverlayService2::class.java
        }
        val intent = Intent(this, service)
        stopService(intent)
    }

    @Composable
    fun OverlayControls(modifier: Modifier = Modifier) {
        val apiTestResult by CarParkRepository.state.collectAsState()
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(300.dp))
            Button(onClick = {
                if (Settings.canDrawOverlays(this@MainActivity)) {
                    startOverlayService(1)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Please grant overlay permission first",
                        Toast.LENGTH_SHORT
                    ).show()
                    requestOverlayPermission()
                }
            }) {
                Text("Show ${ParkingInfo.name1}")
            }

            Button(onClick = {
                stopOverlayService(1)
            }) {
                Text("Hide ${ParkingInfo.name1}")
            }

            Button(onClick = {
                if (Settings.canDrawOverlays(this@MainActivity)) {
                    startOverlayService(2)
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Please grant overlay permission first",
                        Toast.LENGTH_SHORT
                    ).show()
                    requestOverlayPermission()
                }
            }) {
                Text("Show ${ParkingInfo.name2}")
            }

            Button(onClick = {
                stopOverlayService(2)
            }) {
                Text("Hide ${ParkingInfo.name2}")
            }

            Button(onClick = {
                GlobalScope.launch(Dispatchers.IO) {
                    CarParkRepository.fetchCarParkData()
                }
            }) {
                Text("Test Api")
            }

            Text(apiTestResult)
        }
    }
}
