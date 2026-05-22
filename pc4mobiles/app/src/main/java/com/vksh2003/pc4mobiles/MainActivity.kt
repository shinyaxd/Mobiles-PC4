package com.vksh2003.pc4mobiles

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vksh2003.pc4mobiles.data.*
import com.vksh2003.pc4mobiles.ui.theme.Pc4mobilesTheme
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// --- ViewModel insertado aquí para evitar errores de referencia ---
class FlightSearchViewModel(
    private val airportDao: AirportDao,
    private val favoriteDao: FavoriteDao,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    val searchQuery: StateFlow<String> = userPreferencesRepository.searchQuery
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    fun updateSearchQuery(query: String) = viewModelScope.launch { userPreferencesRepository.saveSearchQuery(query) }

    fun searchAirports(query: String): Flow<List<Airport>> {
        Log.d("DEBUG_DB", "Consultando: $query")
        return airportDao.searchAirports(query)
    }

    fun getAvailableFlights(): Flow<List<Airport>> = airportDao.getAllAirports()
    val favoritesList: Flow<List<Favorite>> = favoriteDao.getAllFavorites()

    fun addFavorite(dep: String, dest: String) = viewModelScope.launch { favoriteDao.insert(Favorite(departureCode = dep, destinationCode = dest)) }
    fun removeFavorite(fav: Favorite) = viewModelScope.launch { favoriteDao.delete(fav) }
}

class FlightSearchViewModelFactory(private val airportDao: AirportDao, private val favoriteDao: FavoriteDao, private val prefs: UserPreferencesRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = FlightSearchViewModel(airportDao, favoriteDao, prefs) as T
}

// --- MainActivity ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Pc4mobilesTheme {
                val app = application as FlightApplication
                val viewModel: FlightSearchViewModel = viewModel(factory = FlightSearchViewModelFactory(app.database.airportDao(), app.database.favoriteDao(), app.preferencesRepository))
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    FlightSearchApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun FlightSearchApp(viewModel: FlightSearchViewModel) {
    val savedQuery by viewModel.searchQuery.collectAsState()
    var textInput by remember(savedQuery) { mutableStateOf(savedQuery) }
    val searchResults by viewModel.searchAirports(textInput).collectAsState(initial = emptyList())
    val favorites by viewModel.favoritesList.collectAsState(initial = emptyList())
    val allDestinations by viewModel.getAvailableFlights().collectAsState(initial = emptyList())
    var selectedAirport by remember { mutableStateOf<Airport?>(null) }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(value = textInput, onValueChange = { textInput = it; viewModel.updateSearchQuery(it); selectedAirport = null }, label = { Text("Buscar (Nombre/IATA)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))

        if (textInput.isEmpty()) {
            Text("Rutas Favoritas", style = MaterialTheme.typography.titleLarge)
            LazyColumn { items(favorites) { fav -> FavoriteRouteItem(fav) { viewModel.removeFavorite(fav) } } }
        } else if (selectedAirport == null) {
            LazyColumn { items(searchResults) { airport -> AirportSuggestionItem(airport) { selectedAirport = airport } } }
        } else {
            Text("Vuelos desde ${selectedAirport!!.iataCode}")
            LazyColumn { items(allDestinations.filter { it.iataCode != selectedAirport!!.iataCode }) { dest ->
                FlightRouteItem(selectedAirport!!, dest, favorites.any { it.destinationCode == dest.iataCode }) {
                    if (favorites.any { it.destinationCode == dest.iataCode }) viewModel.removeFavorite(favorites.first { it.destinationCode == dest.iataCode })
                    else viewModel.addFavorite(selectedAirport!!.iataCode, dest.iataCode)
                }
            }}
        }
    }
}

@Composable
fun AirportSuggestionItem(airport: Airport, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = airport.iataCode, fontWeight = FontWeight.Bold)
        Text(text = airport.name, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun FlightRouteItem(departure: Airport, destination: Airport, isFavorite: Boolean, onFavoriteClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "SALIDA", style = MaterialTheme.typography.labelSmall)
                Text(text = "${departure.iataCode} - ${departure.name}")
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "LLEGADA", style = MaterialTheme.typography.labelSmall)
                Text(text = "${destination.iataCode} - ${destination.name}")
            }
            IconButton(onClick = onFavoriteClick) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Toggle Favorite"
                )
            }
        }
    }
}

@Composable
fun FavoriteRouteItem(favorite: Favorite, onRemove: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Ruta:", fontWeight = FontWeight.Bold)
                Text(text = "${favorite.departureCode} -> ${favorite.destinationCode}")
            }
            IconButton(onClick = onRemove) {
                Icon(imageVector = Icons.Filled.Favorite, contentDescription = "Remove Favorite")
            }
        }
    }
}