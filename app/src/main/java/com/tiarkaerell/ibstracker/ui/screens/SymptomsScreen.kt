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
import com.tiarkaerell.ibstracker.ui.viewmodel.SymptomsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SymptomsScreen(symptomsViewModel: SymptomsViewModel) {
    var selectedSymptom by remember { mutableStateOf<String?>(null) }
    var customSymptom by remember { mutableStateOf("") }
    var intensity by remember { mutableStateOf(0f) }
    var selectedDateTime by remember { mutableStateOf(Calendar.getInstance()) }
    val loggedSymptoms by symptomsViewModel.symptoms.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val context = LocalContext.current

    val symptoms = listOf(
        stringResource(R.string.symptom_bloating),
        stringResource(R.string.symptom_abdominal_pain),
        stringResource(R.string.symptom_diarrhea),
        stringResource(R.string.symptom_constipation)
    )
    val otherText = stringResource(R.string.symptom_other)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Input form
        Text(text = stringResource(R.string.symptoms_title), modifier = Modifier.padding(bottom = 16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            symptoms.forEach { symptom ->
                Button(
                    onClick = { selectedSymptom = symptom },
                ) {
                    Text(symptom)
                }
            }
            Button(
                onClick = { selectedSymptom = otherText },
            ) {
                Text(otherText)
            }
        }

        if (selectedSymptom != null) {
            val symptomToSave = if (selectedSymptom == otherText) customSymptom else selectedSymptom

            if (selectedSymptom == otherText) {
                OutlinedTextField(
                    value = customSymptom,
                    onValueChange = { customSymptom = it },
                    label = { Text(stringResource(R.string.symptom_label)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )
            } else {
                Text(text = stringResource(R.string.symptom_selected, selectedSymptom!!), modifier = Modifier.padding(top = 16.dp))
            }

            Text(text = stringResource(R.string.intensity_label, intensity.toInt()), modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            Slider(
                value = intensity,
                onValueChange = { intensity = it },
                valueRange = 0f..10f,
                steps = 9,
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
                    if (symptomToSave != null && symptomToSave.isNotBlank()) {
                        symptomsViewModel.saveSymptom(symptomToSave, intensity.toInt(), selectedDateTime.time)
                        selectedSymptom = null
                        customSymptom = ""
                        intensity = 0f
                        selectedDateTime = Calendar.getInstance()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_save))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // List of symptoms
        if (loggedSymptoms.isNotEmpty()) {
            Text(
                text = "Recent Entries",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(loggedSymptoms.sortedByDescending { it.date }) { symptom ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = symptom.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Intensity: ${symptom.intensity}/10",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dateFormat.format(symptom.date),
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