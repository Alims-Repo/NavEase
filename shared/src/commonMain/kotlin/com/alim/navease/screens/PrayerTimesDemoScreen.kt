package com.alim.navease.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import io.github.alimsrepo.navease.runtime.annotations.NavEaseScreen
import io.github.alimsrepo.navease.runtime.domain.NavScreen
import io.github.alimsrepo.navease.runtime.navigation.NavController

private data class CityPrayers(
    val city: String,
    val emoji: String,
    val method: String,
    val fajr: String, val sunrise: String, val dhuhr: String,
    val asr: String, val maghrib: String, val isha: String,
)

// Realistic pre-computed prayer times (static demo values)
private val cityData = listOf(
    CityPrayers("Makkah", "🕋", "Umm al-Qura",
        "04:47", "06:14", "12:19", "15:39", "18:23", "19:53"),
    CityPrayers("Madinah", "🟢", "Umm al-Qura",
        "04:54", "06:22", "12:26", "15:47", "18:29", "19:59"),
    CityPrayers("London", "🇬🇧", "Muslim World League",
        "03:21", "05:11", "13:02", "17:04", "21:12", "22:55"),
    CityPrayers("New York", "🗽", "ISNA",
        "03:53", "05:42", "12:58", "16:53", "20:12", "21:47"),
    CityPrayers("Jakarta", "🇮🇩", "Kemenag",
        "04:22", "05:38", "11:51", "15:11", "17:48", "19:00"),
)

private val prayerNames = listOf("Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha")

@NavEaseScreen(route = "PrayerTimesDemo")
class PrayerTimesDemoScreen : NavScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content(navKey: NavKey, navController: NavController) {
        var selectedCity by remember { mutableStateOf(cityData[0]) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Prayer Times KMM", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Astronomical calculations demo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        TextButton(onClick = { navController.back() }) { Text("← Back") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // City picker
                item {
                    Text(
                        "SELECT CITY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cityData.forEach { city ->
                            val isSelected = city == selectedCity
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCity = city },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(city.emoji, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        city.city.take(5),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Prayer times card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(selectedCity.emoji, style = MaterialTheme.typography.titleLarge)
                                Column {
                                    Text(
                                        selectedCity.city,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        "Method: ${selectedCity.method}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                            )
                            Spacer(Modifier.height(12.dp))

                            val times = listOf(
                                selectedCity.fajr, selectedCity.sunrise, selectedCity.dhuhr,
                                selectedCity.asr, selectedCity.maghrib, selectedCity.isha
                            )
                            val icons = listOf("🌙", "🌅", "☀️", "🌤", "🌇", "🌑")

                            times.forEachIndexed { i, time ->
                                if (i > 0) Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(icons[i], style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            prayerNames[i],
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (prayerNames[i] == "Sunrise") FontWeight.Normal else FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.let {
                                                if (prayerNames[i] == "Sunrise") it.copy(alpha = 0.65f) else it
                                            }
                                        )
                                    }
                                    Text(
                                        time,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // Code example
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "API USAGE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "val prayerTimes = PrayerTimes(\n" +
                                       "    coordinates = Coordinates(\n" +
                                       "        latitude  = ${
                                           when (selectedCity.city) {
                                               "Makkah"   -> "21.4225, // Makkah"
                                               "Madinah"  -> "24.5247, // Madinah"
                                               "London"   -> "51.5074, // London"
                                               "New York" -> "40.7128, // New York"
                                               else       -> "-6.2088, // Jakarta"
                                           }
                                       }\n" +
                                       "        longitude = ${
                                           when (selectedCity.city) {
                                               "Makkah"   -> "39.8262,"
                                               "Madinah"  -> "39.5692,"
                                               "London"   -> "-0.1278,"
                                               "New York" -> "-74.0060,"
                                               else       -> "106.8456,"
                                           }
                                       }\n" +
                                       "    ),\n" +
                                       "    dateComponents = DateComponents.fromDate(Clock.System.now()),\n" +
                                       "    calculationParameters = CalculationParameters(\n" +
                                       "        method = CalculationMethod.${
                                           when (selectedCity.method) {
                                               "Umm al-Qura"        -> "UMM_AL_QURA"
                                               "Muslim World League" -> "MOON_SIGHTING_COMMITTEE"
                                               "ISNA"               -> "NORTH_AMERICA"
                                               else                 -> "KARACHI"
                                           }
                                       },\n" +
                                       "        madhab = Madhab.SHAFI,\n" +
                                       "    )\n)\n\n" +
                                       "println(prayerTimes.fajr)    // ${selectedCity.fajr}\n" +
                                       "println(prayerTimes.dhuhr)   // ${selectedCity.dhuhr}\n" +
                                       "println(prayerTimes.maghrib) // ${selectedCity.maghrib}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f)
                            )
                        }
                    }
                }

                // Calculation methods
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "11+ CALCULATION METHODS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(10.dp))
                            listOf(
                                "UMM_AL_QURA" to "Umm al-Qura University, Makkah",
                                "MOON_SIGHTING_COMMITTEE" to "Muslim World League",
                                "NORTH_AMERICA" to "Islamic Society of North America (ISNA)",
                                "EGYPTIAN" to "Egyptian General Authority of Survey",
                                "KARACHI" to "University of Islamic Sciences, Karachi",
                                "GULF" to "Gulf Region",
                                "KUWAIT" to "Kuwait",
                                "QATAR" to "Qatar",
                                "SINGAPORE" to "Majlis Ugama Islam Singapura",
                                "IRAN" to "Institute of Geophysics, Tehran",
                                "TURKEY" to "Diyanet İşleri Başkanlığı",
                            ).forEachIndexed { i, (code, name) ->
                                if (i > 0) Spacer(Modifier.height(5.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = code,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1.2f)
                                    )
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

