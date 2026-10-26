
package com.example.gezihava.ui.city

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gezihava.model.City

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityScreen(
    cities: List<City>,
    onCitySelected: (City) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Şehir seç") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cities) { city ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCitySelected(city) }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = city.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Enlem: ${city.latitude}, Boylam: ${city.longitude}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
