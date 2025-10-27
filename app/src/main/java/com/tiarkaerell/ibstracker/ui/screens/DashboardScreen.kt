package com.tiarkaerell.ibstracker.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.R
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.model.FoodUsageStats
import com.tiarkaerell.ibstracker.data.model.Symptom
import com.tiarkaerell.ibstracker.ui.components.QuickAddSection
import com.tiarkaerell.ibstracker.ui.viewmodel.FoodUsageStatsViewModel
import com.tiarkaerell.ibstracker.ui.viewmodel.FoodViewModel
import com.tiarkaerell.ibstracker.ui.viewmodel.SymptomsViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

// Sealed class to represent timeline entries
sealed class TimelineEntry(val date: Date) {
    data class FoodEntry(val foodItem: FoodItem) : TimelineEntry(foodItem.timestamp)
    data class SymptomEntry(val symptom: Symptom) : TimelineEntry(symptom.date)
}

// Helper function to get relative date label
private fun getRelativeDateLabel(date: Date, context: android.content.Context): String {
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
            context.getString(R.string.today)
        }
        entryCalendar.timeInMillis == yesterday.timeInMillis -> {
            context.getString(R.string.yesterday)
        }
        else -> {
            // For dates within the last 7 days, show day name
            val daysDiff = ((today.timeInMillis - entryCalendar.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
            if (daysDiff < 7) {
                val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                dayFormat.format(date)
            } else {
                // For older dates, show full date
                val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    foodViewModel: FoodViewModel,
    symptomsViewModel: SymptomsViewModel,
    foodUsageStatsViewModel: FoodUsageStatsViewModel
) {
    val foodItems by foodViewModel.foodItems.collectAsState()
    val symptoms by symptomsViewModel.symptoms.collectAsState()
    val topUsedFoods by foodUsageStatsViewModel.topUsedFoods.collectAsState()
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val context = LocalContext.current

    // Quick add dialog states
    var showQuickAddDialog by remember { mutableStateOf(false) }
    var quickAddItem by remember { mutableStateOf<Pair<String, com.tiarkaerell.ibstracker.data.model.FoodCategory>?>(null) }
    var quickAddDateTime by remember { mutableStateOf(Calendar.getInstance()) }

    // Dialog states
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<Any?>(null) }
    var showEditFoodDialog by remember { mutableStateOf(false) }
    var showEditSymptomDialog by remember { mutableStateOf(false) }
    var editingFoodItem by remember { mutableStateOf<FoodItem?>(null) }
    var editingSymptom by remember { mutableStateOf<Symptom?>(null) }

    // Delete confirmation dialog
    if (showDeleteDialog && itemToDelete != null) {
        val currentItem = itemToDelete
        val itemName = when (currentItem) {
            is FoodItem -> currentItem.name
            is Symptom -> currentItem.name
            else -> stringResource(R.string.item_generic)
        }
        
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                itemToDelete = null
            },
            title = { Text(stringResource(R.string.delete_entry_title)) },
            text = { Text(stringResource(R.string.delete_entry_message, itemName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        when (currentItem) {
                            is FoodItem -> foodViewModel.deleteFoodItem(currentItem)
                            is Symptom -> symptomsViewModel.deleteSymptom(currentItem)
                        }
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

    // Edit Food dialog
    if (showEditFoodDialog && editingFoodItem != null) {
        var editName by remember { mutableStateOf(editingFoodItem!!.name) }
        var editCategory by remember { mutableStateOf(editingFoodItem!!.category) }
        var showEditCategoryDropdown by remember { mutableStateOf(false) }
        var editDateTime by remember {
            mutableStateOf(Calendar.getInstance().apply { time = editingFoodItem!!.timestamp })
        }

        AlertDialog(
            onDismissRequest = { 
                showEditFoodDialog = false
                editingFoodItem = null
            },
            title = { Text(stringResource(R.string.edit_food_entry_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(stringResource(R.string.food_name_edit_label)) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    // Category dropdown for edit
                    ExposedDropdownMenuBox(
                        expanded = showEditCategoryDropdown,
                        onExpandedChange = { showEditCategoryDropdown = !showEditCategoryDropdown },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        OutlinedTextField(
                            value = editCategory.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.category_title)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showEditCategoryDropdown)
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(editCategory.colorLight)
                                )
                            },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                        )
                        ExposedDropdownMenu(
                            expanded = showEditCategoryDropdown,
                            onDismissRequest = { showEditCategoryDropdown = false }
                        ) {
                            com.tiarkaerell.ibstracker.data.model.FoodCategory.getAllCategories().forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = category.icon,
                                                contentDescription = category.displayName,
                                                tint = category.colorLight,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(category.displayName)
                                        }
                                    },
                                    onClick = {
                                        editCategory = category
                                        showEditCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
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
                            )
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
                        val updatedItem = editingFoodItem!!.copy(
                            name = editName,
                            category = editCategory,
                            timestamp = editDateTime.time
                        )
                        foodViewModel.updateFoodItem(updatedItem)
                        showEditFoodDialog = false
                        editingFoodItem = null
                    }
                ) {
                    Text(stringResource(R.string.button_save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditFoodDialog = false
                        editingFoodItem = null
                    }
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }

    // Edit Symptom dialog
    if (showEditSymptomDialog && editingSymptom != null) {
        var editName by remember { mutableStateOf(editingSymptom!!.name) }
        var editIntensity by remember { mutableStateOf(editingSymptom!!.intensity.toFloat()) }
        var editDateTime by remember { 
            mutableStateOf(Calendar.getInstance().apply { time = editingSymptom!!.date })
        }

        AlertDialog(
            onDismissRequest = { 
                showEditSymptomDialog = false
                editingSymptom = null
            },
            title = { Text(stringResource(R.string.edit_symptom_title)) },
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
                            .combinedClickable(
                                onClick = {
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
                            )
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
                        val updatedItem = editingSymptom!!.copy(
                            name = editName,
                            intensity = editIntensity.roundToInt(),
                            date = editDateTime.time
                        )
                        symptomsViewModel.updateSymptom(updatedItem)
                        showEditSymptomDialog = false
                        editingSymptom = null
                    }
                ) {
                    Text(stringResource(R.string.button_save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditSymptomDialog = false
                        editingSymptom = null
                    }
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }

    // Quick add confirmation dialog
    if (showQuickAddDialog && quickAddItem != null) {
        val (foodName, category) = quickAddItem!!
        var selectedDateTime by remember { mutableStateOf(quickAddDateTime) }

        AlertDialog(
            onDismissRequest = {
                showQuickAddDialog = false
                quickAddItem = null
            },
            title = { Text("Add $foodName") },
            text = {
                Column {
                    Text("Category: ${category.displayName}")
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            selectedDateTime = Calendar.getInstance().apply {
                                                set(year, month, dayOfMonth, hourOfDay, minute)
                                            }
                                        },
                                        selectedDateTime.get(Calendar.HOUR_OF_DAY),
                                        selectedDateTime.get(Calendar.MINUTE),
                                        true
                                    ).show()
                                },
                                selectedDateTime.get(Calendar.YEAR),
                                selectedDateTime.get(Calendar.MONTH),
                                selectedDateTime.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(dateFormat.format(selectedDateTime.time))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        foodViewModel.saveFoodItem(
                            name = foodName,
                            category = category,
                            timestamp = selectedDateTime.time
                        )
                        showQuickAddDialog = false
                        quickAddItem = null
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showQuickAddDialog = false
                        quickAddItem = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

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
        // Quick Add Section
        item {
            QuickAddSection(
                topUsedFoods = topUsedFoods,
                onFoodClick = { foodStats ->
                    quickAddItem = Pair(foodStats.foodName, foodStats.category)
                    quickAddDateTime = Calendar.getInstance()
                    showQuickAddDialog = true
                }
            )
        }

        // Timeline section header
        item {
            Text(
                text = stringResource(R.string.dashboard_recent_activity),
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
                            text = stringResource(R.string.no_entries_yet),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.start_tracking_message),
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
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = getRelativeDateLabel(dayDate, context),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        HorizontalDivider(
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
                        var showOptions by remember { mutableStateOf(false) }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { },
                                    onLongClick = { showOptions = true }
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = item.category.colorLight.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Category icon
                                Icon(
                                    imageVector = item.category.icon,
                                    contentDescription = item.category.displayName,
                                    tint = item.category.colorLight,
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
                                        text = item.category.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = item.category.colorLight
                                    )
                                    Text(
                                        text = timeFormat.format(item.timestamp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                // Edit/Delete buttons (only visible after long press)
                                if (showOptions) {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingFoodItem = item
                                                showEditFoodDialog = true
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
                                                itemToDelete = item
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
                    is TimelineEntry.SymptomEntry -> {
                        val symptom = entry.symptom
                        var showOptions by remember { mutableStateOf(false) }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = { },
                                    onLongClick = { showOptions = true }
                                ),
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
                                    contentDescription = stringResource(R.string.symptom_label_short),
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
                                        text = stringResource(R.string.intensity_scale_format, symptom.intensity),
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
                                
                                // Edit/Delete buttons (only visible after long press)
                                if (showOptions) {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingSymptom = symptom
                                                showEditSymptomDialog = true
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
}