package com.vksh2003.pc4mobiles

import android.app.Application
import com.vksh2003.pc4mobiles.data.db.AppDatabase
import com.vksh2003.pc4mobiles.data.preferences.UserPreferencesRepository
import com.vksh2003.pc4mobiles.data.preferences.dataStore

class FlightApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val preferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(dataStore)
    }
}