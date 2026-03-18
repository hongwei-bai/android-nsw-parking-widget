package com.melonapp.android_nsw_parking_widget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.melonapp.android_nsw_parking_widget.data.DataStoreManager
import com.melonapp.android_nsw_parking_widget.data.repository.CarParkRepository

class CarParkViewModelFactory(
    private val repository: CarParkRepository,
    private val dataStoreManager: DataStoreManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarParkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CarParkViewModel(repository, dataStoreManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
