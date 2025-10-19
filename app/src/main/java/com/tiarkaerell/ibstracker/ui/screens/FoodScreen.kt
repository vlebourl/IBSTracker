package com.tiarkaerell.ibstracker.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.R
import com.tiarkaerell.ibstracker.data.model.FoodCategory
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.ui.viewmodel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FoodScreen(foodViewModel: FoodViewModel) {
    var foodItem by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(FoodCategory.OTHER) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var selectedDateTime by remember { mutableStateOf(Calendar.getInstance()) }
    var editingItem by remember { mutableStateOf<FoodItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<FoodItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var filterCategory by remember { mutableStateOf<FoodCategory?>(null) }
    
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
        var editCategory by remember { mutableStateOf(editingItem!!.category) }
        var editDateTime by remember { 
            mutableStateOf(Calendar.getInstance().apply { time = editingItem!!.date })
        }
        var showEditCategoryDropdown by remember { mutableStateOf(false) }

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
                            label = { Text("Category") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showEditCategoryDropdown)
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(editCategory.color)
                                )
                            },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showEditCategoryDropdown,
                            onDismissRequest = { showEditCategoryDropdown = false }
                        ) {
                            FoodCategory.getAllCategories().forEach { category ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(category.color)
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
                            category = editCategory,
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

        // Category selection dropdown
        ExposedDropdownMenuBox(
            expanded = showCategoryDropdown,
            onExpandedChange = { showCategoryDropdown = !showCategoryDropdown },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                value = selectedCategory.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown)
                },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(selectedCategory.color)
                    )
                },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = showCategoryDropdown,
                onDismissRequest = { showCategoryDropdown = false }
            ) {
                FoodCategory.getAllCategories().forEach { category ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(category.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = category.displayName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = category.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        onClick = {
                            selectedCategory = category
                            showCategoryDropdown = false
                        }
                    )
                }
            }
        }

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
                    foodViewModel.saveFoodItem(foodItem, quantity, selectedCategory, selectedDateTime.time)
                    foodItem = ""
                    quantity = ""
                    selectedCategory = FoodCategory.OTHER
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
            
            // Category filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterCategory == null,
                        onClick = { filterCategory = null },
                        label = { Text("All") }
                    )
                }
                items(FoodCategory.getAllCategories()) { category ->
                    FilterChip(
                        selected = filterCategory == category,
                        onClick = { 
                            filterCategory = if (filterCategory == category) null else category 
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(category.color)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(category.displayName)
                            }
                        }
                    )
                }
            }
            
            val filteredItems = if (filterCategory == null) {
                foodItems
            } else {
                foodItems.filter { it.category == filterCategory }
            }
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredItems.sortedByDescending { it.date }) { item ->
                    var showOptions by remember { mutableStateOf(false) }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { },
                                onLongClick = { showOptions = true }
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = item.category.color.copy(alpha = 0.15f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(item.category.color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    Text(
                                        text = "Quantity: ${item.quantity}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = item.category.displayName,
                                        style = MaterialTheme.typography.bodySmall,
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