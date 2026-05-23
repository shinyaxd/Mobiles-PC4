package com.vksh2003.pc4mobiles.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "flight_search_preferences"
)

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    private companion object {
        val SEARCH_QUERY_KEY = stringPreferencesKey("search_query")
    }

    val searchQuery: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[SEARCH_QUERY_KEY] ?: ""
        }

    suspend fun saveSearchQuery(query: String) {
        dataStore.edit { preferences ->
            preferences[SEARCH_QUERY_KEY] = query
        }
    }
}