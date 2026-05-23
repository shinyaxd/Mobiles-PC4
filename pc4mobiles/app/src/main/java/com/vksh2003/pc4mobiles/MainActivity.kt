package com.vksh2003.pc4mobiles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vksh2003.pc4mobiles.data.entity.Airport
import com.vksh2003.pc4mobiles.data.entity.Favorite
import com.vksh2003.pc4mobiles.ui.theme.Pc4mobilesTheme
import com.vksh2003.pc4mobiles.viewmodel.FlightSearchViewModel
import com.vksh2003.pc4mobiles.viewmodel.FlightSearchViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Pc4mobilesTheme {
                val app = application as FlightApplication
                val viewModel: FlightSearchViewModel = viewModel(
                    factory = FlightSearchViewModelFactory(
                        app.database.airportDao(),
                        app.database.favoriteDao(),
                        app.preferencesRepository
                    )
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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

    val selectedAirport by viewModel.selectedAirport.collectAsState()
    val favorites by viewModel.favoritesList.collectAsState()

    val airportResults by viewModel.searchAirports(textInput).collectAsState(initial = emptyList())

    val destinationResults by selectedAirport?.iataCode
        ?.let { code ->
            viewModel.getDestinationAirports(code).collectAsState(initial = emptyList())
        } ?: remember { mutableStateOf(emptyList()) }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = textInput,
            onValueChange = {
                textInput = it
                viewModel.updateSearchQuery(it)
                if (it.isBlank()) {
                    viewModel.clearSelectedAirport()
                }
            },
            label = { Text("Buscar aeropuerto o código IATA") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            textInput.isBlank() -> {
                Text(
                    text = "Rutas favoritas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(favorites) { favorite ->
                        FavoriteItem(favorite = favorite)
                    }
                }
            }

            selectedAirport == null -> {
                Text(
                    text = "Sugerencias",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(airportResults) { airport ->
                        AirportItem(
                            airport = airport,
                            onClick = {
                                viewModel.selectAirport(airport)
                                textInput = airport.iataCode
                            }
                        )
                    }
                }
            }

            else -> {
                Text(
                    text = "Vuelos desde ${selectedAirport?.iataCode} - ${selectedAirport?.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(destinationResults) { destination ->
                        DestinationItem(
                            departure = selectedAirport!!,
                            destination = destination,
                            onFavoriteClick = {
                                viewModel.addFavorite(
                                    selectedAirport!!.iataCode,
                                    destination.iataCode
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AirportItem(airport: Airport, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = airport.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Text(text = "IATA: ${airport.iataCode}")
            Text(text = "Pasajeros: ${airport.passengers}")
        }
    }
}

@Composable
fun DestinationItem(
    departure: Airport,
    destination: Airport,
    onFavoriteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${departure.iataCode} -> ${destination.iataCode}",
                fontWeight = FontWeight.Bold
            )
            Text(text = destination.name)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onFavoriteClick) {
                Text("Guardar en favoritos")
            }
        }
    }
}

@Composable
fun FavoriteItem(favorite: Favorite) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${favorite.departureCode} -> ${favorite.destinationCode}",
                fontWeight = FontWeight.Bold
            )
        }
    }
}