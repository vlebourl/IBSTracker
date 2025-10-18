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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.R
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.ui.viewmodel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FoodScreen(foodViewModel: FoodViewModel) {
    var foodItem by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedDateTime by remember { mutableStateOf(Calendar.getInstance()) }
    var editingItem by remember { mutableStateOf<FoodItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<FoodItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    
    val foodItems by foodViewModel.foodItems.collectAsState()
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val context = LocalContext.current

    // Delete confirmation dialog
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                itemToDelete = null
            },
            title = { Text("Delete Entry") },
            text = { Text("Are you sure you want to delete '${itemToDelete!!.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        foodViewModel.deleteFoodItem(itemToDelete!!)
                        showDeleteDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit dialog
    if (showEditDialog && editingItem != null) {
        var editName by remember { mutableStateOf(editingItem!!.name) }
        var editQuantity by remember { mutableStateOf(editingItem!!.quantity) }
        var editDateTime by remember { 
            mutableStateOf(Calendar.getInstance().apply { time = editingItem!!.date })
        }

        AlertDialog(
            onDismissRequest = { 
                showEditDialog = false
                editingItem = null
            },
            title = { Text("Edit Entry") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Food Name") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = editQuantity,
                        onValueChange = { editQuantity = it },
                        label = { Text("Quantity") },
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
                                contentDescription = "Select date",
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
                            quantity = editQuantity,
                            date = editDateTime.time
                        )
                        foodViewModel.updateFoodItem(updatedItem)
                        showEditDialog = false
                        editingItem = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                        editingItem = null
                    }
                ) {
                    Text("Cancel")
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
                                
                                if (showOptions) {
                                    Row {
                                        IconButton(
                                            onClick = {
                                                editingItem = item
                                                showEditDialog = true
                                                showOptions = false
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Edit,
                                                contentDescription = "Edit",
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
                                                contentDescription = "Delete",
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