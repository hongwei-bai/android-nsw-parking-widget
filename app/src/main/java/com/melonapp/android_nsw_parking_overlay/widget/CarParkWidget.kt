package com.melonapp.android_nsw_parking_overlay.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.background
import androidx.glance.text.FontWeight
import com.melonapp.android_nsw_parking_overlay.data.DataStoreManager
import com.melonapp.android_nsw_parking_overlay.data.api.RetrofitClient
import com.melonapp.android_nsw_parking_overlay.data.repository.CarParkRepository
import com.melonapp.android_nsw_parking_overlay.ui.SelectedCarPark
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.first

class CarParkWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dataStoreManager = DataStoreManager(context)
        val repository = CarParkRepository(RetrofitClient.apiService)
        val gson = Gson()

        val apiKey = dataStoreManager.apiKey.first()
        val selectedJson = dataStoreManager.selectedCarParks.first()
        
        val selectedCarParks: List<SelectedCarPark> = if (!selectedJson.isNullOrBlank()) {
            val type = object : TypeToken<List<SelectedCarPark>>() {}.type
            gson.fromJson(selectedJson, type)
        } else {
            emptyList()
        }

        val updatedCarParks = if (!apiKey.isNullOrBlank() && apiKey != "YOUR_API_KEY_HERE") {
            selectedCarParks.map { carPark ->
                try {
                    val details = repository.getCarParkDetails(apiKey, carPark.id)
                    carPark.copy(availableSpots = details?.availableSpots ?: 0)
                } catch (e: Exception) {
                    carPark
                }
            }
        } else {
            selectedCarParks
        }

        provideContent {
            CarParkWidgetContent(updatedCarParks, apiKey.isNullOrBlank() || apiKey == "YOUR_API_KEY_HERE")
        }
    }

    @Composable
    private fun CarParkWidgetContent(carParks: List<SelectedCarPark>, isApiKeyMissing: Boolean) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(android.R.color.background_dark))
                .padding(8.dp),
            verticalAlignment = Alignment.Vertical.Top,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "Parking Availability",
                    style = TextStyle(
                        color = ColorProvider(android.R.color.white),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            if (isApiKeyMissing) {
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Set API Key in App",
                        style = TextStyle(color = ColorProvider(android.R.color.holo_red_light))
                    )
                }
            } else if (carParks.isEmpty()) {
                Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No car parks selected",
                        style = TextStyle(color = ColorProvider(android.R.color.darker_gray))
                    )
                }
            } else {
                carParks.forEach { carPark ->
                    CarParkItem(carPark)
                }
            }
        }
    }

    @Composable
    private fun CarParkItem(carPark: SelectedCarPark) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .background(ColorProvider(android.R.color.black))
                .padding(8.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.Start
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = carPark.abbr,
                    style = TextStyle(
                        color = ColorProvider(android.R.color.white),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = carPark.name,
                    style = TextStyle(
                        color = ColorProvider(android.R.color.darker_gray),
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
            }
            
            val spotColor = when {
                carPark.availableSpots > 50 -> android.R.color.holo_green_light
                carPark.availableSpots > 10 -> android.R.color.holo_orange_light
                else -> android.R.color.holo_red_light
            }

            Text(
                text = carPark.availableSpots.toString(),
                style = TextStyle(
                    color = ColorProvider(spotColor),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
