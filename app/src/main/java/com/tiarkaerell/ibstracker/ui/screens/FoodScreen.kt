package com.tiarkaerell.ibstracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.ui.viewmodel.FoodViewModel

@Composable
fun FoodScreen(foodViewModel: FoodViewModel) {
    var foodItem by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Log Food or Drink", modifier = Modifier.padding(bottom = 16.dp))
        OutlinedTextField(
            value = foodItem,
            onValueChange = { foodItem = it },
            label = { Text("Food or Drink") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Quantity") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )
        Button(
            onClick = { 
                if (foodItem.isNotBlank() && quantity.isNotBlank()) {
                    foodViewModel.saveFoodItem(foodItem, quantity)
                    foodItem = ""
                    quantity = ""
                }
             },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}