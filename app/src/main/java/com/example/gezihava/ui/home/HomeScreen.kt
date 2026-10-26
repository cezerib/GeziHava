
package com.example.gezihava.ui.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gezihava.data.CityRepository
import com.example.gezihava.data.WeatherRepository
import com.example.gezihava.model.City
import com.example.gezihava.model.District
import com.example.gezihava.model.TouristPlace
import com.example.gezihava.model.WeatherResponse
import com.example.gezihava.ui.DistrictScreen
import com.example.gezihava.ui.city.CityScreen
import com.example.gezihava.ui.tourist.TouristPlaceScreen
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DarkBlue = Color(0xFF173B63)
private val LightBlue = Color(0xFFE5F2FF)
private val ScreenBackground = Color(0xFFF7FAFE)

@Composable
fun HomeScreen() {
    val cityRepository = remember { CityRepository() }
    val weatherRepository = remember { WeatherRepository() }
    val scope = rememberCoroutineScope()

    var showCityScreen by remember { mutableStateOf(false) }
    var showDistrictScreen by remember { mutableStateOf(false) }
    var showTouristPlacesScreen by remember { mutableStateOf(false) }

    var selectedDistrict by remember {
        mutableStateOf<District?>(null)
    }

    var selectedCity by remember {
        mutableStateOf(
            City(
                id = 16,
                name = "Bursa",
                latitude = 40.195,
                longitude = 29.060
            )
        )
    }

    var weatherLatitude by remember { mutableDoubleStateOf(40.195) }
    var weatherLongitude by remember { mutableDoubleStateOf(29.060) }
    var weatherLocationName by remember { mutableStateOf("Bursa") }

    var cities by remember { mutableStateOf<List<City>>(emptyList()) }
    var citiesLoading by remember { mutableStateOf(true) }
    var citiesError by remember { mutableStateOf<String?>(null) }

    var weather by remember { mutableStateOf<WeatherResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshCounter by remember { mutableIntStateOf(0) }

    // Türkiye illerini API'den getir.
    LaunchedEffect(Unit) {
        citiesLoading = true
        citiesError = null

        cityRepository.getCities()
            .onSuccess { result ->
                cities = result
            }
            .onFailure { error ->
                citiesError = error.message ?: "İller yüklenemedi."
            }

        citiesLoading = false
    }

    // Seçilen konumun hava durumunu getir.
    LaunchedEffect(
        weatherLatitude,
        weatherLongitude,
        refreshCounter
    ) {
        if (weatherLatitude == 0.0 || weatherLongitude == 0.0) {
            isLoading = true
            errorMessage = null

            cityRepository.getCityCoordinates(selectedCity)
                .onSuccess { locatedCity ->
                    selectedCity = locatedCity
                    weatherLatitude = locatedCity.latitude
                    weatherLongitude = locatedCity.longitude
                    weatherLocationName = locatedCity.name
                }
                .onFailure { error ->
                    errorMessage = error.message
                        ?: "Konum koordinatları alınamadı."
                }

            isLoading = false
            return@LaunchedEffect
        }

        isLoading = true
        errorMessage = null

        weatherRepository.getWeather(
            latitude = weatherLatitude,
            longitude = weatherLongitude
        ).onSuccess { response ->
            weather = response
        }.onFailure { error ->
            weather = null
            errorMessage = error.message ?: "Hava durumu alınamadı."
        }

        isLoading = false
    }

    // Tarihî yerler ekranı.
    if (showTouristPlacesScreen) {
        TouristPlaceScreen(
            cityName = selectedCity.name,
            districtName = selectedDistrict?.name ?: "",
            latitude = weatherLatitude,
            longitude = weatherLongitude,
            onPlaceSelected = { place: TouristPlace ->
                weatherLatitude = place.latitude
                weatherLongitude = place.longitude
                weatherLocationName = place.name

                weather = null
                errorMessage = null
                showTouristPlacesScreen = false
                refreshCounter++
            },
            onBack = {
                showTouristPlacesScreen = false
            }
        )
        return
    }

    // İlçe seçim ekranı.
    if (showDistrictScreen) {
        DistrictScreen(
            provinceId = selectedCity.id,
            provinceName = selectedCity.name,
            onDistrictSelected = { district ->
                scope.launch {
                    isLoading = true
                    errorMessage = null

                    cityRepository.getDistrictCoordinates(
                        district = district,
                        provinceName = selectedCity.name
                    ).onSuccess { locatedDistrict ->
                        if (
                            locatedDistrict.latitude == 0.0 ||
                            locatedDistrict.longitude == 0.0
                        ) {
                            errorMessage =
                                "${district.name} için geçerli koordinat bulunamadı."
                        } else {
                            selectedDistrict = locatedDistrict
                            weatherLatitude = locatedDistrict.latitude
                            weatherLongitude = locatedDistrict.longitude
                            weatherLocationName =
                                "${locatedDistrict.name}, ${selectedCity.name}"

                            weather = null
                            showDistrictScreen = false
                            refreshCounter++
                        }
                    }.onFailure { error ->
                        errorMessage = error.message
                            ?: "İlçe koordinatları alınamadı."
                    }

                    isLoading = false
                }
            }
        )
        return
    }

    // İl seçim ekranı.
    if (showCityScreen) {
        when {
            citiesLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            citiesError != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(citiesError ?: "İller yüklenemedi.")
                    Spacer(Modifier.height(12.dp))

                    Button(onClick = { showCityScreen = false }) {
                        Text("Ana sayfaya dön")
                    }
                }
            }

            else -> {
                CityScreen(
                    cities = cities,
                    onCitySelected = { city ->
                        selectedCity = city
                        selectedDistrict = null

                        weatherLatitude = city.latitude
                        weatherLongitude = city.longitude
                        weatherLocationName = city.name

                        weather = null
                        errorMessage = null

                        showCityScreen = false
                        showDistrictScreen = true
                    }
                )
            }
        }
        return
    }

    Scaffold(
        containerColor = ScreenBackground,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = true,
                    onClick = {
                        showTouristPlacesScreen = false
                        showCityScreen = false
                        showDistrictScreen = false
                    },
                    icon = {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Ana sayfa"
                        )
                    },
                    label = { Text("Ana Sayfa") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = {
                        showTouristPlacesScreen = true
                    },
                    icon = { Text("🗺️") },
                    label = { Text("Keşfet") }
                )
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(18.dp))

            Text(
                text = "☀️ GeziHava",
                fontSize = 27.sp,
                color = DarkBlue
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Türkiye'yi hava durumuna göre keşfet",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(18.dp))

            Button(
                onClick = { showCityScreen = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("📍 İl / ilçe seç: ${selectedCity.name}")
            }

            selectedDistrict?.let { district ->
                Spacer(Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Seçilen ilçe",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = district.name,
                                color = DarkBlue,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        TextButton(
                            onClick = { showDistrictScreen = true }
                        ) {
                            Text("Değiştir")
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // Güncel hava durumu.
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = LightBlue
                )
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        text = "${weatherLocationName.uppercase()} · HAVA DURUMU",
                        color = DarkBlue,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(Modifier.height(12.dp))

                    when {
                        isLoading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("Hava durumu yükleniyor...")
                            }
                        }

                        errorMessage != null -> {
                            Text(
                                text = errorMessage ?: "",
                                color = Color.Red
                            )

                            Spacer(Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { refreshCounter++ }
                            ) {
                                Text("Tekrar dene")
                            }
                        }

                        weather?.current != null -> {
                            val current = weather!!.current!!

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = weatherEmoji(current.weatherCode),
                                    fontSize = 48.sp
                                )

                                Spacer(Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = current.temperature
                                            ?.let { "${it.toInt()}°C" }
                                            ?: "--°C",
                                        fontSize = 38.sp,
                                        color = DarkBlue
                                    )

                                    Text(
                                        text = weatherDescription(current.weatherCode),
                                        color = DarkBlue
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = Color.White)
                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "🌡️ Hissedilen: ${
                                    current.apparentTemperature
                                        ?.let { "${it.toInt()}°C" } ?: "--"
                                }",
                                color = DarkBlue
                            )

                            Text(
                                text = "💧 Nem: ${
                                    current.humidity?.let { "%$it" } ?: "--"
                                }",
                                color = DarkBlue
                            )

                            Text(
                                text = "🌧️ Yağış: ${
                                    current.precipitation?.let { "$it mm" } ?: "--"
                                }",
                                color = DarkBlue
                            )

                            Text(
                                text = "💨 Rüzgâr: ${
                                    current.windSpeed?.let { "$it km/sa" } ?: "--"
                                }",
                                color = DarkBlue
                            )

                            Spacer(Modifier.height(8.dp))

                            Text(
                                text = "Kaynak: Open-Meteo",
                                fontSize = 11.sp,
                                color = DarkBlue
                            )
                        }

                        else -> {
                            Text(
                                text = "Hava durumu bekleniyor...",
                                color = DarkBlue
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "7 Günlük Hava Tahmini",
                fontSize = 20.sp,
                color = DarkBlue
            )

            Spacer(Modifier.height(12.dp))

            val daily = weather?.daily

            if (daily?.dates != null) {
                daily.dates.indices.forEach { index ->
                    val date = daily.dates[index]
                    val code = daily.weatherCodes?.getOrNull(index)
                    val maxTemp = daily.maxTemperatures?.getOrNull(index)
                    val minTemp = daily.minTemperatures?.getOrNull(index)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 1.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = formatForecastDate(date),
                                    color = DarkBlue,
                                    style = MaterialTheme.typography.titleSmall
                                )

                                Text(
                                    text = weatherDescription(code),
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Text(
                                text = weatherEmoji(code),
                                fontSize = 26.sp
                            )

                            Spacer(Modifier.width(12.dp))

                            Text(
                                text = "${maxTemp?.toInt()?.let { "$it°" } ?: "--"} / " +
                                        "${minTemp?.toInt()?.let { "$it°" } ?: "--"}",
                                color = DarkBlue,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            } else if (!isLoading && errorMessage == null) {
                Text(
                    text = "Tahmin verisi henüz yüklenmedi.",
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(24.dp))

            // Yalnızca Tarihî Yerler kategorisi.
            Text(
                text = "Kategoriler",
                fontSize = 20.sp,
                color = DarkBlue
            )

            Spacer(Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        showTouristPlacesScreen = true
                    },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 2.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏛️", fontSize = 32.sp)

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Tarihî Yerler",
                            color = DarkBlue,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Tarihî yapıları keşfet ve hava durumunu öğren.",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatForecastDate(date: String): String {
    return try {
        val parsedDate = LocalDate.parse(date)

        val dayName = parsedDate.dayOfWeek.getDisplayName(
            java.time.format.TextStyle.SHORT,
            Locale("tr", "TR")
        ).replaceFirstChar {
            it.uppercase(Locale("tr", "TR"))
        }

        val dayMonth = parsedDate.format(
            DateTimeFormatter.ofPattern("d MMM", Locale("tr", "TR"))
        )

        "$dayName · $dayMonth"
    } catch (e: Exception) {
        date
    }
}

private fun weatherEmoji(code: Int?): String {
    return when (code) {
        0 -> "☀️"
        1, 2 -> "🌤️"
        3 -> "☁️"
        45, 48 -> "🌫️"
        51, 53, 55, 56, 57 -> "🌦️"
        61, 63, 65, 66, 67 -> "🌧️"
        71, 73, 75, 77 -> "❄️"
        80, 81, 82 -> "🌧️"
        85, 86 -> "🌨️"
        95, 96, 99 -> "⛈️"
        else -> "🌡️"
    }
}

private fun weatherDescription(code: Int?): String {
    return when (code) {
        0 -> "Açık"
        1 -> "Çoğunlukla açık"
        2 -> "Parçalı bulutlu"
        3 -> "Kapalı"
        45, 48 -> "Sisli"
        51, 53, 55, 56, 57 -> "Çisenti"
        61, 63, 65, 66, 67 -> "Yağmurlu"
        71, 73, 75, 77 -> "Karlı"
        80, 81, 82 -> "Sağanak yağış"
        85, 86 -> "Kar sağanağı"
        95, 96, 99 -> "Gök gürültülü fırtına"
        else -> "Bilinmeyen hava durumu"
    }
}
