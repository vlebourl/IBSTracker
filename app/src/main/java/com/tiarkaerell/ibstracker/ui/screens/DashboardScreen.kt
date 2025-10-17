package com.tiarkaerell.ibstracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Welcome to IBS Tracker", modifier = Modifier.padding(bottom = 16.dp))
        Text(text = "Here you will see a summary of your recent food and symptom logs.")
    }
}