package com.tiarkaerell.ibstracker.ui.components.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tiarkaerell.ibstracker.data.model.AnalysisTimeWindow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DateRangePickerDialog(
    currentTimeWindow: AnalysisTimeWindow,
    onTimeWindowChange: (AnalysisTimeWindow) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var startDate by remember { mutableStateOf(currentTimeWindow.startDate) }
    var endDate by remember { mutableStateOf(currentTimeWindow.endDate) }
    var selectedPreset by remember { mutableStateOf(getPresetForTimeWindow(currentTimeWindow)) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Date Range",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close dialog"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Preset options
                Text(
                    text = "Quick Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    DatePresetOption(
                        title = "Last 7 days",
                        description = "Past week",
                        isSelected = selectedPreset == "7_days",
                        onClick = {
                            selectedPreset = "7_days"
                            endDate = LocalDate.now()
                            startDate = endDate.minusDays(7)
                        }
                    )
                    
                    DatePresetOption(
                        title = "Last 14 days",
                        description = "Past 2 weeks",
                        isSelected = selectedPreset == "14_days",
                        onClick = {
                            selectedPreset = "14_days"
                            endDate = LocalDate.now()
                            startDate = endDate.minusDays(14)
                        }
                    )
                    
                    DatePresetOption(
                        title = "Last 30 days",
                        description = "Past month",
                        isSelected = selectedPreset == "30_days",
                        onClick = {
                            selectedPreset = "30_days"
                            endDate = LocalDate.now()
                            startDate = endDate.minusDays(30)
                        }
                    )
                    
                    DatePresetOption(
                        title = "Last 60 days",
                        description = "Past 2 months",
                        isSelected = selectedPreset == "60_days",
                        onClick = {
                            selectedPreset = "60_days"
                            endDate = LocalDate.now()
                            startDate = endDate.minusDays(60)
                        }
                    )
                    
                    DatePresetOption(
                        title = "Last 90 days",
                        description = "Past 3 months",
                        isSelected = selectedPreset == "90_days",
                        onClick = {
                            selectedPreset = "90_days"
                            endDate = LocalDate.now()
                            startDate = endDate.minusDays(90)
                        }
                    )

                    DatePresetOption(
                        title = "All Time",
                        description = "Entire history",
                        isSelected = selectedPreset == "all_time",
                        onClick = {
                            selectedPreset = "all_time"
                            endDate = LocalDate.now()
                            // Go back 10 years - should cover any realistic data
                            startDate = endDate.minusYears(10)
                        }
                    )

                    DatePresetOption(
                        title = "Custom Range",
                        description = "Choose specific dates",
                        isSelected = selectedPreset == "custom",
                        onClick = {
                            selectedPreset = "custom"
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Custom date selection (only shown when custom is selected)
                if (selectedPreset == "custom") {
                    CustomDateSelection(
                        startDate = startDate,
                        endDate = endDate,
                        onStartDateChange = { startDate = it },
                        onEndDateChange = { endDate = it }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Date range summary
                DateRangeSummary(
                    startDate = startDate,
                    endDate = endDate
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            val newTimeWindow = currentTimeWindow.copy(
                                startDate = startDate,
                                endDate = endDate
                            )
                            onTimeWindowChange(newTimeWindow)
                            onDismiss()
                        },
                        enabled = startDate.isBefore(endDate) || startDate.isEqual(endDate)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun DatePresetOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun CustomDateSelection(
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Custom Date Range",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Start date
            DateFieldRow(
                label = "Start Date",
                date = startDate,
                onDateChange = onStartDateChange
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // End date
            DateFieldRow(
                label = "End Date",
                date = endDate,
                onDateChange = onEndDateChange
            )
            
            // Validation message
            if (startDate.isAfter(endDate)) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Start date must be before or equal to end date",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun DateFieldRow(
    label: String,
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(80.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        OutlinedTextField(
            value = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
            onValueChange = { /* Read-only for now */ },
            readOnly = true,
            modifier = Modifier.weight(1f),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select date"
                )
            },
            singleLine = true
        )
        
        // Note: In a real implementation, you would integrate with a date picker library
        // or Android's DatePickerDialog for proper date selection
    }
}

@Composable
private fun DateRangeSummary(
    startDate: LocalDate,
    endDate: LocalDate
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Selected Range",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${startDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))} - ${endDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1
            Text(
                text = "$daysBetween days of data",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

private fun getPresetForTimeWindow(timeWindow: AnalysisTimeWindow): String {
    val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(timeWindow.startDate, timeWindow.endDate)
    // Check if it's "All Time" (roughly 10 years)
    if (daysBetween >= 3650) {
        return "all_time"
    }
    return when (daysBetween) {
        7L -> "7_days"
        14L -> "14_days"
        30L -> "30_days"
        60L -> "60_days"
        90L -> "90_days"
        else -> "custom"
    }
}