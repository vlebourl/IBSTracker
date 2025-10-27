package com.tiarkaerell.ibstracker.ui.components.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.data.model.TriggerProbability
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CorrelationMetrics(
    trigger: TriggerProbability,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Correlation Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Main probability display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Trigger Probability",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "${trigger.probabilityPercentage}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = getProbabilityColor(trigger.probability)
                    )
                }
                
                // Circular progress indicator
                CircularCorrelationChart(
                    probability = trigger.probability,
                    confidence = trigger.confidence,
                    modifier = Modifier.size(80.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Metric breakdown
            MetricBreakdown(trigger)
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Confidence indicator
            ConfidenceIndicator(trigger.confidence)
        }
    }
}

@Composable
private fun CircularCorrelationChart(
    probability: Double,
    confidence: Double,
    modifier: Modifier = Modifier
) {
    val primaryColor = getProbabilityColor(probability)
    val confidenceColor = MaterialTheme.colorScheme.outline
    
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f - 8.dp.toPx()
        
        // Background circle
        drawCircle(
            color = confidenceColor.copy(alpha = 0.2f),
            radius = radius,
            center = center
        )
        
        // Probability arc
        drawProbabilityArc(
            probability = probability,
            center = center,
            radius = radius,
            color = primaryColor
        )
        
        // Confidence indicator (outer ring)
        drawConfidenceRing(
            confidence = confidence,
            center = center,
            radius = radius + 4.dp.toPx(),
            color = confidenceColor
        )
    }
}

private fun DrawScope.drawProbabilityArc(
    probability: Double,
    center: Offset,
    radius: Float,
    color: Color
) {
    val sweepAngle = (probability * 360).toFloat()
    
    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
    )
}

private fun DrawScope.drawConfidenceRing(
    confidence: Double,
    center: Offset,
    radius: Float,
    color: Color
) {
    val points = 12
    val angleStep = 2 * PI / points
    val dotRadius = 2.dp.toPx()
    
    for (i in 0 until points) {
        val angle = i * angleStep
        val alpha = if (i < confidence * points) 1f else 0.3f
        
        val x = center.x + radius * cos(angle - PI / 2).toFloat()
        val y = center.y + radius * sin(angle - PI / 2).toFloat()
        
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = dotRadius,
            center = Offset(x, y)
        )
    }
}

@Composable
private fun MetricBreakdown(trigger: TriggerProbability) {
    Column {
        MetricBar(
            label = "Temporal",
            value = trigger.temporalScore,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        MetricBar(
            label = "Baseline",
            value = trigger.baselineScore,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        MetricBar(
            label = "Frequency",
            value = trigger.frequencyScore,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun MetricBar(
    label: String,
    value: Double,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(60.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            // Background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        color = color.copy(alpha = 0.2f),
                        size = size
                    )
                    
                    drawRect(
                        color = color,
                        size = androidx.compose.ui.geometry.Size(
                            width = size.width * value.toFloat(),
                            height = size.height
                        )
                    )
                }
            }
        }
        
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(40.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ConfidenceIndicator(confidence: Double) {
    val confidenceColor = getConfidenceColor(confidence)
    val outlineColor = MaterialTheme.colorScheme.outline
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Confidence:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Confidence dots
        Row {
            repeat(5) { index ->
                val filled = index < (confidence * 5).toInt()
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = if (filled) {
                                confidenceColor
                            } else {
                                outlineColor.copy(alpha = 0.3f)
                            },
                            radius = size.minDimension / 2
                        )
                    }
                }
                
                if (index < 4) {
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Text(
            text = "${(confidence * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = confidenceColor
        )
    }
}

private fun getProbabilityColor(probability: Double): Color {
    return when {
        probability >= 0.7 -> Color(0xFFD32F2F) // Red - High probability
        probability >= 0.4 -> Color(0xFFFF9800) // Orange - Medium probability  
        else -> Color(0xFF4CAF50) // Green - Low probability
    }
}

@Composable
private fun getConfidenceColor(confidence: Double): Color {
    return when {
        confidence >= 0.7 -> MaterialTheme.colorScheme.primary
        confidence >= 0.4 -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
}