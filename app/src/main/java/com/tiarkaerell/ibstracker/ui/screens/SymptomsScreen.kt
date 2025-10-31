package com.tiarkaerell.ibstracker.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.R
import com.tiarkaerell.ibstracker.data.model.Symptom
import com.tiarkaerell.ibstracker.ui.viewmodel.SymptomsUiState
import com.tiarkaerell.ibstracker.ui.viewmodel.SymptomsViewModel
import com.tiarkaerell.ibstracker.ui.viewmodel.ValidationResult
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SymptomsScreen(symptomsViewModel: SymptomsViewModel) {
    var selectedSymptom by remember { mutableStateOf<String?>(null) }
    var customSymptom by remember { mutableStateOf("") }
    var intensity by remember { mutableStateOf(0f) }
    var selectedDateTime by remember { mutableStateOf(Calendar.getInstance()) }
    var editingItem by remember { mutableStateOf<Symptom?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Symptom?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    val loggedSymptoms by symptomsViewModel.symptoms.collectAsState()
    val uiState by symptomsViewModel.uiState.collectAsStateWithLifecycle()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val context = LocalContext.current

    val symptoms = listOf(
        stringResource(R.string.symptom_bloating),
        stringResource(R.string.symptom_abdominal_pain),
        stringResource(R.string.symptom_diarrhea),
        stringResource(R.string.symptom_constipation)
    )
    val otherText = stringResource(R.string.symptom_other)

    // Delete confirmation dialog
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                itemToDelete = null
            },
            title = { Text(stringResource(R.string.delete_symptom_title)) },
            text = { Text(stringResource(R.string.delete_symptom_message, itemToDelete!!.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        symptomsViewModel.deleteSymptom(itemToDelete!!)
                        showDeleteDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.button_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }

    // Error dialog
    when (val state = uiState) {
        is SymptomsUiState.Error -> {
            AlertDialog(
                onDismissRequest = { symptomsViewModel.dismissMessage() },
                title = { Text("Error") },
                text = { Text(state.message) },
                confirmButton = {
                    TextButton(onClick = { symptomsViewModel.dismissMessage() }) {
                        Text("OK")
                    }
                }
            )
        }
        else -> {}
    }

    // Edit dialog
    if (showEditDialog && editingItem != null) {
        var editName by remember { mutableStateOf(editingItem!!.name) }
        var editIntensity by remember { mutableStateOf(editingItem!!.intensity.toFloat()) }
        var editDateTime by remember { 
            mutableStateOf(Calendar.getInstance().apply { time = editingItem!!.date })
        }

        AlertDialog(
            onDismissRequest = { 
                showEditDialog = false
                editingItem = null
            },
            title = { Text(stringResource(R.string.edit_symptom_entry_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(stringResource(R.string.symptom_name_label)) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = stringResource(R.string.intensity_format, editIntensity.roundToInt()),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Slider(
                        value = editIntensity,
                        onValueChange = { editIntensity = it },
                        valueRange = 0f..10f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val newCalendar = Calendar.getInstance().apply {
                                            set(year, month, dayOfMonth, 
                                                editDateTime.get(Calendar.HOUR_OF_DAY), 
                                                editDateTime.get(Calendar.MINUTE))
                                        }
                                        editDateTime = newCalendar
                                        
                                        TimePickerDialog(
                                            context,
                                            { _, hourOfDay, minute ->
                                                val finalCalendar = Calendar.getInstance().apply {
                                                    set(year, month, dayOfMonth, hourOfDay, minute)
                                                }
                                                editDateTime = finalCalendar
                                            },
                                            editDateTime.get(Calendar.HOUR_OF_DAY),
                                            editDateTime.get(Calendar.MINUTE),
                                            true
                                        ).show()
                                    },
                                    editDateTime.get(Calendar.YEAR),
                                    editDateTime.get(Calendar.MONTH),
                                    editDateTime.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFormat.format(editDateTime.time),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = stringResource(R.string.cd_select_date),
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updatedItem = editingItem!!.copy(
                            name = editName,
                            intensity = editIntensity.roundToInt(),
                            date = editDateTime.time
                        )
                        symptomsViewModel.updateSymptom(updatedItem)
                        showEditDialog = false
                        editingItem = null
                    }
                ) {
                    Text(stringResource(R.string.button_save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        editingItem = null
                    }
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }

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
                FilterChip(
                    selected = selectedSymptom == symptom,
                    onClick = { 
                        selectedSymptom = if (selectedSymptom == symptom) null else symptom
                        customSymptom = ""
                    },
                    label = { Text(symptom) }
                )
            }
            FilterChip(
                selected = selectedSymptom == otherText,
                onClick = { 
                    selectedSymptom = if (selectedSymptom == otherText) null else otherText
                    customSymptom = ""
                },
                label = { Text(otherText) }
            )
        }

        if (selectedSymptom == otherText) {
            OutlinedTextField(
                value = customSymptom,
                onValueChange = { customSymptom = it },
                label = { Text(stringResource(R.string.symptom_custom_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = stringResource(R.string.symptom_intensity_label) + ": ${intensity.roundToInt()}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Slider(
            value = intensity,
            onValueChange = { intensity = it },
            valueRange = 0f..10f,
            steps = 9,
            modifier = Modifier.fillMaxWidth()
        )
        
        // Date/Time Picker
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 16.dp)
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
                        text = stringResource(R.string.date_time_label),
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
                    contentDescription = stringResource(R.string.cd_select_date_time_short),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Button(
            onClick = {
                val symptomName = when {
                    selectedSymptom == otherText && customSymptom.isNotBlank() -> customSymptom
                    selectedSymptom != null && selectedSymptom != otherText -> selectedSymptom!!
                    else -> null
                }

                if (symptomName != null) {
                    // Validate before saving
                    val nameValidation = symptomsViewModel.validateSymptomName(symptomName)
                    val intensityValidation = symptomsViewModel.validateIntensity(intensity.roundToInt())
                    val timestampValidation = symptomsViewModel.validateTimestamp(selectedDateTime.time)

                    when {
                        nameValidation is ValidationResult.Invalid -> {
                            android.widget.Toast.makeText(context, nameValidation.error, android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        intensityValidation is ValidationResult.Invalid -> {
                            android.widget.Toast.makeText(context, intensityValidation.error, android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        timestampValidation is ValidationResult.Invalid -> {
                            android.widget.Toast.makeText(context, timestampValidation.error, android.widget.Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        else -> {
                            // All validations passed, save symptom
                            symptomsViewModel.saveSymptom(
                                symptomName,
                                intensity.roundToInt(),
                                selectedDateTime.time
                            )
                            selectedSymptom = null
                            customSymptom = ""
                            intensity = 0f
                            selectedDateTime = Calendar.getInstance()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.button_save))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // List of logged symptoms
        if (loggedSymptoms.isNotEmpty()) {
            Text(
                text = stringResource(R.string.recent_symptoms_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(loggedSymptoms.sortedByDescending { it.date }) { symptom ->
                    var showOptions by remember { mutableStateOf(false) }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { },
                                onLongClick = { showOptions = true }
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = symptom.name,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = stringResource(R.string.intensity_scale_format, symptom.intensity),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = dateFormat.format(symptom.date),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                if (showOptions) {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingItem = symptom
                                                showEditDialog = true
                                                showOptions = false
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = stringResource(R.string.cd_edit),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                itemToDelete = symptom
                                                showDeleteDialog = true
                                                showOptions = false
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.cd_delete),
                                                tint = MaterialTheme.colorScheme.error
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
}