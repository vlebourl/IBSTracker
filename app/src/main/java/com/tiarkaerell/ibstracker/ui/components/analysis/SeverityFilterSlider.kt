package com.tiarkaerell.ibstracker.ui.components.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SeverityFilterSlider(
    currentThreshold: Int?,
    onThresholdChange: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderValue by remember { 
        mutableFloatStateOf(currentThreshold?.toFloat() ?: 0f) 
    }
    val isFilterActive = currentThreshold != null
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Minimum Severity Filter",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Switch(
                checked = isFilterActive,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        val threshold = if (sliderValue > 0) sliderValue.toInt() else 3
                        onThresholdChange(threshold)
                        sliderValue = threshold.toFloat()
                    } else {
                        onThresholdChange(null)
                    }
                }
            )
        }
        
        if (isFilterActive) {
            Spacer(modifier = Modifier.height(16.dp))
            
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
                        text = "Show symptoms with severity ≥ ${sliderValue.toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Slider(
                        value = sliderValue,
                        onValueChange = { value ->
                            sliderValue = value
                        },
                        onValueChangeFinished = {
                            onThresholdChange(sliderValue.toInt())
                        },
                        valueRange = 1f..10f,
                        steps = 8, // 1,2,3,4,5,6,7,8,9,10
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "1",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Mild",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "5",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Moderate",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "10",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Severe",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val severityDescription = when (sliderValue.toInt()) {
                        in 1..3 -> "Including mild symptoms and above"
                        in 4..6 -> "Including moderate symptoms and above"
                        in 7..8 -> "Including severe symptoms only"
                        in 9..10 -> "Including only the most severe symptoms"
                        else -> "All severity levels"
                    }
                    
                    Text(
                        text = severityDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Show symptoms of all severity levels",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}