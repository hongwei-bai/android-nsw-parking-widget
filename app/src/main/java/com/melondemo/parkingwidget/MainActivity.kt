package com.melondemo.parkingwidget

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

        // Use Scaffold for top overlay space + bottom controls
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { handleOverlayAction(1, true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFCA840)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show ${ParkingInfo.name1}", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { handleOverlayAction(1, false) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0D1B41)),
                        border = BorderStroke(1.dp, Color(0xFFFCA840)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hide ${ParkingInfo.name1}")
                    }

                    Button(
                        onClick = { handleOverlayAction(2, true) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD83B92)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Show ${ParkingInfo.name2}", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = { handleOverlayAction(2, false) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0D1B41)),
                        border = BorderStroke(1.dp, Color(0xFFD83B92)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Hide ${ParkingInfo.name2}")
                    }

                    Button(
                        onClick = {
                            GlobalScope.launch(Dispatchers.IO) {
                                CarParkRepository.fetchCarParkData()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1BB39E)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Test API", color = Color.White)
                    }

                    Text(
                        apiTestResult,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        ) { innerPadding ->
            // Top empty space for overlay
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.Transparent)
            ) {
                // Could show an app title or branding here
                Text(
                    text = "🍈 Melon Parking App",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
        }
    }

    private fun Context.handleOverlayAction(slot: Int, show: Boolean) {
        if (Settings.canDrawOverlays(this)) {
            if (show) startOverlayService(slot) else stopOverlayService(slot)
        } else {
            Toast.makeText(this, "Please grant overlay permission first", Toast.LENGTH_SHORT).show()
            requestOverlayPermission()
        }
    }
}
