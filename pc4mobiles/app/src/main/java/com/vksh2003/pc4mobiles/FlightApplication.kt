package com.vksh2003.pc4mobiles

import android.app.Application
import com.vksh2003.pc4mobiles.data.AppDatabase
import com.vksh2003.pc4mobiles.data.UserPreferencesRepository
import com.vksh2003.pc4mobiles.data.dataStore

class FlightApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val preferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(dataStore)
    }
}