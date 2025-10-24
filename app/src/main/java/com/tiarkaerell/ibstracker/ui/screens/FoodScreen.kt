package com.tiarkaerell.ibstracker.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.R
import com.tiarkaerell.ibstracker.data.model.CommonFood
import com.tiarkaerell.ibstracker.data.model.CommonFoods
import com.tiarkaerell.ibstracker.data.model.FoodCategory
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.model.FoodUsageStats
import com.tiarkaerell.ibstracker.ui.components.QuickAddSection
import com.tiarkaerell.ibstracker.ui.viewmodel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FoodScreen(foodViewModel: FoodViewModel) {
    var selectedCategory by remember { mutableStateOf<FoodCategory?>(null) }
    var customFoodName by remember { mutableStateOf("") }
    var showCustomInput by remember { mutableStateOf(false) }
    var selectedDateTime by remember { mutableStateOf(Calendar.getInstance()) }
    var editingItem by remember { mutableStateOf<FoodItem?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<FoodItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var filterCategory by remember { mutableStateOf<FoodCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<CommonFood>>(emptyList()) }
    var showQuickAddDialog by remember { mutableStateOf(false) }
    var quickAddItem by remember { mutableStateOf<Pair<String, FoodCategory>?>(null) }
    var quickAddDateTime by remember { mutableStateOf(Calendar.getInstance()) }

    val foodItems by foodViewModel.foodItems.collectAsState()
    val topUsedFoods by foodViewModel.getTopUsedFoods().collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val context = LocalContext.current

    // Reactive search using database DAO
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            foodViewModel.searchCommonFoods(searchQuery).collect { results ->
                searchResults = results
            }
        } else {
            searchResults = emptyList()
        }
    }

    // Quick Add confirmation dialog
    if (showQuickAddDialog && quickAddItem != null) {
        AlertDialog(
            onDismissRequest = {
                showQuickAddDialog = false
                quickAddItem = null
                quickAddDateTime = Calendar.getInstance()
                selectedDateTime = Calendar.getInstance()
            },
            title = { Text(stringResource(R.string.quick_add_confirm_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.quick_add_confirm_message, quickAddItem!!.first),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Date/Time picker
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val newCalendar = Calendar.getInstance()
                                        newCalendar.timeInMillis = quickAddDateTime.timeInMillis
                                        newCalendar.set(Calendar.YEAR, year)
                                        newCalendar.set(Calendar.MONTH, month)
                                        newCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                                        TimePickerDialog(
                                            context,
                                            { _, hourOfDay, minute ->
                                                val updatedCalendar = Calendar.getInstance()
                                                updatedCalendar.timeInMillis = newCalendar.timeInMillis
                                                updatedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                                updatedCalendar.set(Calendar.MINUTE, minute)
                                                quickAddDateTime = updatedCalendar // Reassign to trigger recomposition
                                            },
                                            newCalendar.get(Calendar.HOUR_OF_DAY),
                                            newCalendar.get(Calendar.MINUTE),
                                            true
                                        ).show()
                                    },
                                    quickAddDateTime.get(Calendar.YEAR),
                                    quickAddDateTime.get(Calendar.MONTH),
                                    quickAddDateTime.get(Calendar.DAY_OF_MONTH)
                                ).show()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = stringResource(R.string.cd_select_date_time)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(dateFormat.format(quickAddDateTime.time))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        foodViewModel.saveFoodItem(
                            name = quickAddItem!!.first,
                            category = quickAddItem!!.second,
                            timestamp = quickAddDateTime.time
                        )
                        showQuickAddDialog = false
                        quickAddItem = null
                        quickAddDateTime = Calendar.getInstance()
                        selectedDateTime = Calendar.getInstance()
                    }
                ) {
                    Text(stringResource(R.string.button_add))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showQuickAddDialog = false
                        quickAddItem = null
                        quickAddDateTime = Calendar.getInstance()
                        selectedDateTime = Calendar.getInstance()
                    }
                ) {
                    Text(stringResource(R.string.button_cancel))
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                itemToDelete = null
            },
            title = { Text(stringResource(R.string.delete_food_title)) },
            text = { Text(stringResource(R.string.delete_food_message, itemToDelete!!.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        foodViewModel.deleteFoodItem(itemToDelete!!)
                        showDeleteDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.button_delete), color = MaterialTheme.colorScheme.error)
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

    // Edit dialog
    if (showEditDialog && editingItem != null) {
        var editName by remember { mutableStateOf(editingItem!!.name) }
        var editCategory by remember { mutableStateOf(editingItem!!.category) }
        var editDateTime by remember {
            mutableStateOf(Calendar.getInstance().apply { time = editingItem!!.timestamp })
        }

        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
                editingItem = null
            },
            title = { Text(stringResource(R.string.edit_food_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(stringResource(R.string.food_name_edit_label)) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    // Category selection for edit
                    Text(
                        text = stringResource(R.string.category_title),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(100.dp)
                    ) {
                        items(FoodCategory.getAllCategories()) { category ->
                            FilterChip(
                                selected = editCategory == category,
                                onClick = { editCategory = category },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(category.colorLight)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            category.displayName,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                },
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        editDateTime.set(Calendar.YEAR, year)
                                        editDateTime.set(Calendar.MONTH, month)
                                        editDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        
                                        TimePickerDialog(
                                            context,
                                            { _, hourOfDay, minute ->
                                                editDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                                editDateTime.set(Calendar.MINUTE, minute)
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
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = stringResource(R.string.cd_select_date_time)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(dateFormat.format(editDateTime.time))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updatedItem = editingItem!!.copy(
                            name = editName,
                            category = editCategory,
                            timestamp = editDateTime.time
                        )
                        foodViewModel.updateFoodItem(updatedItem)
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Add Section (Top 6 most-used foods)
        item {
            QuickAddSection(
                topUsedFoods = topUsedFoods,
                onFoodClick = { foodStats ->
                    // When clicking a food from quick add, show confirmation dialog
                    quickAddItem = Pair(foodStats.foodName, foodStats.category)
                    quickAddDateTime = Calendar.getInstance()
                    showQuickAddDialog = true
                }
            )
        }

        // Entry Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (selectedCategory == null && searchQuery.isEmpty()) 
                            stringResource(R.string.search_food_or_select_category) 
                        else if (selectedCategory == null && searchQuery.isNotEmpty())
                            stringResource(R.string.search_results)
                        else if (!showCustomInput) 
                            stringResource(R.string.choose_food_or_enter_custom)
                        else 
                            stringResource(R.string.enter_food_name_prompt),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Search Bar
                    if (selectedCategory == null) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { query ->
                                searchQuery = query
                            },
                            label = { Text(stringResource(R.string.search_placeholder)) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.cd_search))
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { 
                                        searchQuery = ""
                                        searchResults = emptyList()
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.cd_clear))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    // Search Results or Category Selection Grid
                    if (selectedCategory == null) {
                        if (searchQuery.isNotEmpty() && searchResults.isNotEmpty()) {
                            // Show search results
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.height(200.dp)
                            ) {
                                items(searchResults) { result ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                quickAddItem = Pair(result.name, result.category)
                                                quickAddDateTime = selectedDateTime
                                                showQuickAddDialog = true
                                                searchQuery = ""
                                                searchResults = emptyList()
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = result.category.colorLight.copy(alpha = 0.2f)
                                        ),
                                        border = BorderStroke(1.dp, result.category.colorLight)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(result.category.colorLight)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = result.category.displayName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = result.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (searchQuery.isNotEmpty() && searchResults.isEmpty()) {
                            // No search results found
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = stringResource(R.string.no_foods_found, searchQuery),
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            customFoodName = searchQuery
                                            selectedCategory = FoodCategory.OTHER
                                            showCustomInput = true
                                            searchQuery = ""
                                            searchResults = emptyList()
                                        }
                                    ) {
                                        Text(stringResource(R.string.add_as_other_format, searchQuery))
                                    }
                                }
                            }
                        } else {
                            // Show category grid when no search - all 12 categories visible (4 rows × 80dp + 3 gaps × 8dp = 344dp)
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.height(344.dp)
                            ) {
                            items(FoodCategory.getAllCategories()) { category ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(80.dp)
                                        .clickable { selectedCategory = category },
                                    colors = CardDefaults.cardColors(
                                        containerColor = category.colorLight.copy(alpha = 0.2f)
                                    ),
                                    border = BorderStroke(2.dp, category.colorLight)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = category.icon,
                                            contentDescription = category.displayName,
                                            tint = category.colorLight,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = category.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            textAlign = TextAlign.Center,
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                            }
                        }
                    }

                    // Step 2: Common Foods or Custom Input
                    selectedCategory?.let { category ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssistChip(
                                onClick = { selectedCategory = null; showCustomInput = false; customFoodName = "" },
                                label = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(category.displayName)
                                    }
                                },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(category.colorLight)
                                    )
                                }
                            )
                            
                            if (!showCustomInput) {
                                TextButton(onClick = { showCustomInput = true }) {
                                    Text(stringResource(R.string.enter_custom_button))
                                }
                            }
                        }

                        if (!showCustomInput) {
                            // Show common foods grid from database
                            val commonFoods by foodViewModel.getCommonFoodsByCategory(category).collectAsState(initial = emptyList())
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.height(200.dp)
                            ) {
                                items(commonFoods) { commonFood ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                quickAddItem = Pair(commonFood.name, category)
                                                quickAddDateTime = selectedDateTime
                                                showQuickAddDialog = true
                                                selectedCategory = null
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                    ) {
                                        Text(
                                            text = commonFood.name,
                                            modifier = Modifier.padding(12.dp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            // Show custom input field
                            OutlinedTextField(
                                value = customFoodName,
                                onValueChange = { customFoodName = it },
                                label = { Text(stringResource(R.string.food_name_input)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                trailingIcon = {
                                    if (customFoodName.isNotEmpty()) {
                                        IconButton(onClick = { customFoodName = "" }) {
                                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.cd_clear))
                                        }
                                    }
                                }
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showCustomInput = false; customFoodName = "" },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.back_to_suggestions_button))
                                }
                                
                                Button(
                                    onClick = {
                                        if (customFoodName.isNotBlank()) {
                                            quickAddItem = Pair(customFoodName, category)
                                            quickAddDateTime = selectedDateTime
                                            showQuickAddDialog = true
                                            customFoodName = ""
                                            selectedCategory = null
                                            showCustomInput = false
                                        }
                                    },
                                    enabled = customFoodName.isNotBlank(),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.button_save))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Recent Entries Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.recent_entries),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Category filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterCategory == null,
                        onClick = { filterCategory = null },
                        label = { Text(stringResource(R.string.all_filter)) }
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
                                        .background(category.colorLight)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(category.displayName)
                            }
                        }
                    )
                }
            }
        }

        val filteredItems = if (filterCategory == null) {
            foodItems
        } else {
            foodItems.filter { it.category == filterCategory }
        }

        items(filteredItems.sortedByDescending { it.timestamp }) { item ->
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
                                        .background(item.category.colorLight)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Text(
                                text = item.category.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = dateFormat.format(item.timestamp),
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
        }

        if (filteredItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = stringResource(R.string.no_food_entries),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}