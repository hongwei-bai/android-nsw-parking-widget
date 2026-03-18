package com.melonapp.android_nsw_parking_widget.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "parking_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        val API_KEY = stringPreferencesKey("api_key")
        val SELECTED_CAR_PARKS = stringPreferencesKey("selected_car_parks")
    }

    val apiKey: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[API_KEY]
    }

    val selectedCarParks: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_CAR_PARKS]
    }

    suspend fun saveApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = key
        }
    }

    suspend fun saveSelectedCarParks(json: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_CAR_PARKS] = json
        }
    }
}
