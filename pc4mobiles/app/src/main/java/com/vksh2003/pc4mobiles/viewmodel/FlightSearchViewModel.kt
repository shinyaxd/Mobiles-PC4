package com.vksh2003.pc4mobiles.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vksh2003.pc4mobiles.data.dao.AirportDao
import com.vksh2003.pc4mobiles.data.dao.FavoriteDao
import com.vksh2003.pc4mobiles.data.entity.Airport
import com.vksh2003.pc4mobiles.data.entity.Favorite
import com.vksh2003.pc4mobiles.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.*
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

    private val _selectedAirport = MutableStateFlow<Airport?>(null)
    val selectedAirport: StateFlow<Airport?> = _selectedAirport.asStateFlow()

    val favoritesList: StateFlow<List<Favorite>> = favoriteDao.getAllFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        viewModelScope.launch {
            userPreferencesRepository.saveSearchQuery(query)
        }
    }

    fun selectAirport(airport: Airport) {
        _selectedAirport.value = airport
        updateSearchQuery(airport.iataCode)
    }

    fun clearSelectedAirport() {
        _selectedAirport.value = null
    }

    fun searchAirports(query: String): Flow<List<Airport>> {
        return airportDao.searchAirports(query)
    }

    fun getDestinationAirports(departureCode: String): Flow<List<Airport>> {
        return airportDao.getDestinationAirports(departureCode)
    }

    fun addFavorite(departure: String, destination: String) {
        viewModelScope.launch {
            val existing = favoriteDao.getFavoriteByCodes(departure, destination)
            if (existing == null) {
                favoriteDao.insert(
                    Favorite(
                        departureCode = departure,
                        destinationCode = destination
                    )
                )
            }
        }
    }

    fun removeFavoriteByCodes(departure: String, destination: String) {
        viewModelScope.launch {
            val favorite = favoriteDao.getFavoriteByCodes(departure, destination)
            if (favorite != null) {
                favoriteDao.delete(favorite)
            }
        }
    }

    suspend fun isFavorite(departure: String, destination: String): Boolean {
        return favoriteDao.getFavoriteByCodes(departure, destination) != null
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
            return FlightSearchViewModel(
                airportDao,
                favoriteDao,
                userPreferencesRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}