
package com.example.gezihava.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gezihava.data.CityRepository
import com.example.gezihava.model.District
import kotlinx.coroutines.launch

@Composable
fun DistrictScreen(
    provinceId: Int,
    provinceName: String,
    onDistrictSelected: (District) -> Unit
) {
    val repository = remember { CityRepository() }
    val scope = rememberCoroutineScope()

    var districts by remember { mutableStateOf<List<District>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(provinceId) {
        loading = true
        error = null

        val result = repository.getDistricts(provinceId)

        result.onSuccess {
            districts = it
        }.onFailure {
            error = it.message ?: "İlçeler yüklenemedi."
        }

        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "$provinceName İlçeleri",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            loading -> CircularProgressIndicator()

            error != null -> Text(
                text = error!!,
                color = MaterialTheme.colorScheme.error
            )

            districts.isEmpty() -> Text("Bu il için ilçe bulunamadı.")

            else -> LazyColumn {
                items(districts) { district ->
                    ListItem(
                        headlineContent = { Text(district.name) },
                        trailingContent = { Text("›") },
                        modifier = Modifier.clickable {
                            onDistrictSelected(district)
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
