package com.vksh2003.pc4mobiles.uidata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vksh2003.pc4mobiles.data.Airport
import com.vksh2003.pc4mobiles.data.AirportDao
import com.vksh2003.pc4mobiles.data.Favorite
import com.vksh2003.pc4mobiles.data.FavoriteDao
import com.vksh2003.pc4mobiles.data.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FlightSearchViewModel(
    private val airportDao: AirportDao,
    private val favoriteDao: FavoriteDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val searchQuery: StateFlow<String> = userPreferencesRepository.searchQuery
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveSearchQuery(query)
        }
    }

    fun searchAirports(query: String): Flow<List<Airport>> {
        return airportDao.searchAirports(query)
    }

    fun getAvailableFlights(): Flow<List<Airport>> {
        return airportDao.getAllAirports()
    }

    val favoritesList: Flow<List<Favorite>> = favoriteDao.getAllFavorites()

    fun addFavorite(departureCode: String, destinationCode: String) {
        viewModelScope.launch {
            favoriteDao.insert(Favorite(departureCode = departureCode, destinationCode = destinationCode))
        }
    }

    fun removeFavorite(favorite: Favorite) {
        viewModelScope.launch {
            favoriteDao.delete(favorite)
        }
    }
}

class FlightSearchViewModelFactory(
    private val airportDao: AirportDao,
    private val favoriteDao: FavoriteDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlightSearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FlightSearchViewModel(airportDao, favoriteDao, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}