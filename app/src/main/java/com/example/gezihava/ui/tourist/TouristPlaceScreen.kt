
package com.example.gezihava.ui.tourist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gezihava.data.TouristPlaceRepository
import com.example.gezihava.model.TouristPlace
import kotlinx.coroutines.withTimeout

@Composable
fun TouristPlaceScreen(
    cityName: String,
    districtName: String,
    latitude: Double,
    longitude: Double,
    onPlaceSelected: (TouristPlace) -> Unit,
    onBack: () -> Unit
) {
    val repository = remember {
        TouristPlaceRepository()
    }

    var places by remember {
        mutableStateOf<List<TouristPlace>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    var retryCount by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(cityName, districtName, latitude, longitude, retryCount)  {
        isLoading = true
        errorMessage = null
        places = emptyList()

        try {
            val result = withTimeout(15_000L) {
                repository.getTouristPlaces(
                    latitude = latitude,
                    longitude = longitude,
                    radiusMeters = 3000
                )
            }

            result
                .onSuccess { foundPlaces ->
                    places = foundPlaces.map { place ->
                        place.copy(
                            cityName = cityName,
                            districtName = districtName
                        )
                    }

                    if (places.isEmpty()) {
                        errorMessage =
                            "Bu konumda kayıtlı turistik yer bulunamadı."
                    }
                }
                .onFailure { error ->
                    errorMessage = error.message
                        ?: "Turistik yerler yüklenemedi."
                }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            errorMessage =
                "İstek 45 saniyede tamamlanamadı. " +
                        "Sunucu yoğun olabilir. Biraz bekleyip tekrar deneyin."
        } catch (e: Exception) {
            errorMessage = e.message
                ?: "Bağlantı kurulamadı. İnternet bağlantınızı kontrol edin."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedButton(onClick = onBack) {
            Text("← Geri")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Turistik Yerler",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = listOf(cityName, districtName)
                .filter { it.isNotBlank() }
                .joinToString(" · "),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Turistik yerler aranıyor...")

                    Text(
                        text = "Bu işlem biraz sürebilir.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { retryCount++ }
                ) {
                    Text("Tekrar dene")
                }
            }

            else -> {
                Text(
                    text = "${places.size} yer bulundu",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = places,
                        key = { "${it.id}_${it.category}" }
                    ) { place ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPlaceSelected(place)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = place.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        text = place.category,
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    if (place.description.isNotBlank()) {
                                        Text(
                                            text = place.description,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }

                                Text("›")
                            }
                        }
                    }
                }
            }
        }
    }
}
