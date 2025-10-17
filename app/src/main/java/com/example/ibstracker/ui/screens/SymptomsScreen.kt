package com.example.ibstracker.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SymptomsScreen() {
    var selectedSymptom by remember { mutableStateOf<String?>(null) }
    var customSymptom by remember { mutableStateOf("") }
    var intensity by remember { mutableStateOf(0f) }

    val symptoms = listOf("Bloating", "Abdominal Pain", "Diarrhea", "Constipation")

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Log a Symptom", modifier = Modifier.padding(bottom = 16.dp))

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
                onClick = { selectedSymptom = "Other" },
            ) {
                Text("Other")
            }
        }

        if (selectedSymptom != null) {
            if (selectedSymptom == "Other") {
                OutlinedTextField(
                    value = customSymptom,
                    onValueChange = { customSymptom = it },
                    label = { Text("Symptom") },
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                )
            } else {
                Text(text = "Symptom: $selectedSymptom", modifier = Modifier.padding(top = 16.dp))
            }

            Text(text = "Intensity: ${intensity.toInt()}", modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
            Slider(
                value = intensity,
                onValueChange = { intensity = it },
                valueRange = 0f..10f,
                steps = 9,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )
            Button(
                onClick = { /* TODO: Save the symptom entry */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}