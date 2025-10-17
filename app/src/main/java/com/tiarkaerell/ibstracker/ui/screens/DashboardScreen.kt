package com.tiarkaerell.ibstracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.R
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.model.Symptom
import com.tiarkaerell.ibstracker.ui.viewmodel.FoodViewModel
import com.tiarkaerell.ibstracker.ui.viewmodel.SymptomsViewModel
import java.text.SimpleDateFormat
import java.util.*

// Sealed class to represent timeline entries
sealed class TimelineEntry(val date: Date) {
    data class FoodEntry(val foodItem: FoodItem) : TimelineEntry(foodItem.date)
    data class SymptomEntry(val symptom: Symptom) : TimelineEntry(symptom.date)
}

// Helper function to get relative date label
private fun getRelativeDateLabel(date: Date, locale: Locale): String {
    val calendar = Calendar.getInstance()
    val today = calendar.clone() as Calendar
    today.set(Calendar.HOUR_OF_DAY, 0)
    today.set(Calendar.MINUTE, 0)
    today.set(Calendar.SECOND, 0)
    today.set(Calendar.MILLISECOND, 0)

    val yesterday = today.clone() as Calendar
    yesterday.add(Calendar.DAY_OF_YEAR, -1)

    val entryCalendar = Calendar.getInstance()
    entryCalendar.time = date
    entryCalendar.set(Calendar.HOUR_OF_DAY, 0)
    entryCalendar.set(Calendar.MINUTE, 0)
    entryCalendar.set(Calendar.SECOND, 0)
    entryCalendar.set(Calendar.MILLISECOND, 0)

    return when {
        entryCalendar.timeInMillis == today.timeInMillis -> {
            if (locale.language == "fr") "Aujourd'hui" else "Today"
        }
        entryCalendar.timeInMillis == yesterday.timeInMillis -> {
            if (locale.language == "fr") "Hier" else "Yesterday"
        }
        else -> {
            // For dates within the last 7 days, show day name
            val daysDiff = ((today.timeInMillis - entryCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            if (daysDiff < 7) {
                val dayFormat = SimpleDateFormat("EEEE", locale)
                dayFormat.format(date)
            } else {
                // For older dates, show full date
                val dateFormat = SimpleDateFormat("MMMM dd, yyyy", locale)
                dateFormat.format(date)
            }
        }
    }
}

// Helper function to normalize date to start of day
private fun Date.startOfDay(): Date {
    val calendar = Calendar.getInstance()
    calendar.time = this
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

@Composable
fun DashboardScreen(
    foodViewModel: FoodViewModel,
    symptomsViewModel: SymptomsViewModel
) {
    val foodItems by foodViewModel.foodItems.collectAsState()
    val symptoms by symptomsViewModel.symptoms.collectAsState()
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val locale = Locale.getDefault()

    // Combine, sort, and group entries by day
    val groupedEntries = remember(foodItems, symptoms) {
        val entries = mutableListOf<TimelineEntry>()
        entries.addAll(foodItems.map { TimelineEntry.FoodEntry(it) })
        entries.addAll(symptoms.map { TimelineEntry.SymptomEntry(it) })

        entries
            .sortedByDescending { it.date }
            .groupBy { it.date.startOfDay() }
            .toList()
            .sortedByDescending { it.first }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timeline section header
        item {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Empty state
        if (groupedEntries.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No entries yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start tracking your food and symptoms",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Unified timeline with day separators
            groupedEntries.forEach { (dayDate, entriesForDay) ->
                // Day header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = getRelativeDateLabel(dayDate, locale),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }

                // Entries for this day
                items(entriesForDay) { entry ->
                when (entry) {
                    is TimelineEntry.FoodEntry -> {
                        val item = entry.foodItem
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF88B06D).copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Icon
                                Icon(
                                    imageVector = Icons.Default.Fastfood,
                                    contentDescription = "Food",
                                    tint = Color(0xFF88B06D),
                                    modifier = Modifier.size(32.dp)
                                )

                                // Content
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Quantity: ${item.quantity}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = timeFormat.format(item.date),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    is TimelineEntry.SymptomEntry -> {
                        val symptom = entry.symptom
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFF6B6B).copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Icon
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = "Symptom",
                                    tint = Color(0xFFFF6B6B),
                                    modifier = Modifier.size(32.dp)
                                )

                                // Content
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = symptom.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Intensity: ${symptom.intensity}/10",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = timeFormat.format(symptom.date),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}