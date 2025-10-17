package com.tiarkaerell.ibstracker.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.R
import com.tiarkaerell.ibstracker.ui.viewmodel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FoodScreen(foodViewModel: FoodViewModel) {
    var foodItem by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedDateTime by remember { mutableStateOf(Calendar.getInstance()) }
    val foodItems by foodViewModel.foodItems.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Input form
        Text(text = stringResource(R.string.food_title), modifier = Modifier.padding(bottom = 16.dp))
        OutlinedTextField(
            value = foodItem,
            onValueChange = { foodItem = it },
            label = { Text(stringResource(R.string.food_name_label)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text(stringResource(R.string.food_quantity_label)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        // Date/Time Picker
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable {
                    val calendar = selectedDateTime
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val newCalendar = Calendar.getInstance().apply {
                                set(year, month, dayOfMonth, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
                            }
                            selectedDateTime = newCalendar

                            // After date is selected, show time picker
                            TimePickerDialog(
                                context,
                                { _, hourOfDay, minute ->
                                    val finalCalendar = Calendar.getInstance().apply {
                                        set(year, month, dayOfMonth, hourOfDay, minute)
                                    }
                                    selectedDateTime = finalCalendar
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                true
                            ).show()
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Date & Time",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = dateFormat.format(selectedDateTime.time),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select date and time",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Button(
            onClick = {
                if (foodItem.isNotBlank() && quantity.isNotBlank()) {
                    foodViewModel.saveFoodItem(foodItem, quantity, selectedDateTime.time)
                    foodItem = ""
                    quantity = ""
                    selectedDateTime = Calendar.getInstance()
                }
             },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.button_save))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // List of food items
        if (foodItems.isNotEmpty()) {
            Text(
                text = "Recent Entries",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(foodItems.sortedByDescending { it.date }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Quantity: ${item.quantity}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dateFormat.format(item.date),
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