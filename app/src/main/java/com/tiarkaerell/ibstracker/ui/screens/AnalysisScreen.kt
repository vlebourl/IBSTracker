package com.tiarkaerell.ibstracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tiarkaerell.ibstracker.data.model.*
import com.tiarkaerell.ibstracker.ui.viewmodel.AnalyticsViewModel
import com.tiarkaerell.ibstracker.ui.components.analysis.FilterChips
import com.tiarkaerell.ibstracker.ui.components.analysis.DateRangePickerDialog
import com.tiarkaerell.ibstracker.ui.components.analysis.FilterPresets
import com.tiarkaerell.ibstracker.ui.components.analysis.FilterPresetsDialog
import com.tiarkaerell.ibstracker.ui.components.analysis.TriggerDetailsDialog
import com.tiarkaerell.ibstracker.ui.components.analysis.InsightsTab
import com.tiarkaerell.ibstracker.ui.components.analysis.PatternsTab
import com.tiarkaerell.ibstracker.ui.state.AnalysisFilterState
import com.tiarkaerell.ibstracker.ui.state.rememberAnalysisFilterState
import com.tiarkaerell.ibstracker.IBSTrackerApplication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(analyticsViewModel: AnalyticsViewModel) {
    val analysisResult by analyticsViewModel.analysisResult.collectAsState()
    val isLoading by analyticsViewModel.isLoading.collectAsState()
    val errorMessage by analyticsViewModel.errorMessage.collectAsState()
    val currentFilters by analyticsViewModel.currentFilters.collectAsState()
    val currentTimeWindow by analyticsViewModel.currentTimeWindow.collectAsState()
    
    val filterState = rememberAnalysisFilterState()
    
    // Onboarding state
    var showOnboarding by remember { mutableStateOf(false) }
    var currentTooltipIndex by remember { mutableStateOf(0) }
    
    // Check if this is first time on analysis screen
    LaunchedEffect(Unit) {
        // TODO: Check SharedPreferences for first-time user
        // For now, show onboarding if there's no analysis result
        showOnboarding = analysisResult == null && !isLoading
    }
    
    // Sync filter state with ViewModel
    LaunchedEffect(currentFilters) {
        filterState.filters = currentFilters
    }
    
    LaunchedEffect(currentTimeWindow) {
        filterState.timeWindow = currentTimeWindow
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with refresh and help buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Symptom Analysis",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Row {
                // Help button
                var showHelpDialog by remember { mutableStateOf(false) }
                
                IconButton(onClick = { showHelpDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = "Help",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Refresh button
                IconButton(
                    onClick = { analyticsViewModel.refreshAnalysis() },
                    enabled = !isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Analysis"
                    )
                }
                
                // Help Dialog
                if (showHelpDialog) {
                    AnalysisHelpDialog(onDismiss = { showHelpDialog = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Show Overview first if analysis is available
        if (analysisResult != null && errorMessage == null && !isLoading) {
            OverviewStatsCard(analysisResult!!)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Advanced Filter Controls
        FilterChips(
            filters = filterState.filters,
            onFiltersChange = { newFilters ->
                filterState.filters = newFilters
                analyticsViewModel.updateFilters(newFilters)
            },
            onShowQuickFilters = { filterState.showQuickFiltersDialog() },
            onShowDateRangePicker = { filterState.openDateRangeDialog() },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Date Range Picker Dialog
        if (filterState.isDateRangeDialogOpen) {
            DateRangePickerDialog(
                currentTimeWindow = filterState.timeWindow,
                onTimeWindowChange = { newTimeWindow ->
                    filterState.updateTimeWindow(newTimeWindow)
                    analyticsViewModel.updateTimeWindow(newTimeWindow)
                },
                onDismiss = { filterState.closeDateRangeDialog() }
            )
        }

        // Quick Filters Dialog
        if (filterState.isQuickFiltersDialogOpen) {
            FilterPresetsDialog(
                filterState = filterState,
                onApplyFilter = { analyticsViewModel.updateFilters(filterState.filters) },
                onDismiss = { filterState.closeQuickFiltersDialog() }
            )
        }

        when {
            isLoading -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            contentDescription = "Loading analysis results"
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Analyzing symptom patterns...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            errorMessage != null -> {
                ErrorState(
                    message = errorMessage!!,
                    onRetry = { analyticsViewModel.refreshAnalysis() },
                    onDismiss = { analyticsViewModel.clearError() }
                )
            }
            analysisResult != null -> {
                AnalysisContent(
                    analysisResult = analysisResult!!,
                    analyticsViewModel = analyticsViewModel
                )
            }
            else -> {
                EmptyAnalysisState()
            }
        }
        
        // Onboarding tooltips overlay
        if (showOnboarding) {
            OnboardingTooltipsOverlay(
                currentTooltipIndex = currentTooltipIndex,
                onNextTooltip = { currentTooltipIndex++ },
                onPreviousTooltip = { if (currentTooltipIndex > 0) currentTooltipIndex-- },
                onFinishOnboarding = { 
                    showOnboarding = false 
                    currentTooltipIndex = 0
                }
            )
        }
    }
}


@Composable
private fun AnalysisContent(
    analysisResult: AnalysisResult,
    analyticsViewModel: AnalyticsViewModel
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    Column {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        text = "Symptoms",
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Text(
                        text = "Patterns",
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Timeline,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = {
                    Text(
                        text = "Insights",
                        fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal
                    )
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Content
        when (selectedTabIndex) {
            0 -> SymptomsTabContent(analysisResult)
            1 -> PatternsTab(
                analysisResult = analysisResult,
                symptoms = emptyList<com.tiarkaerell.ibstracker.data.analysis.SymptomOccurrence>(),
                foods = emptyList<com.tiarkaerell.ibstracker.data.analysis.FoodOccurrence>(),
                modifier = Modifier.weight(1f)
            )
            2 -> InsightsTab(
                analysisResult = analysisResult,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SymptomsTabContent(analysisResult: AnalysisResult) {
    var selectedSymptom by remember { mutableStateOf<SymptomAnalysis?>(null) }

    if (analysisResult.symptomAnalyses.isEmpty()) {
        NoSymptomsEmptyState()
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Grid layout - 2 items per row
            items(
                count = (analysisResult.symptomAnalyses.size + 1) / 2,
                key = { index -> "row_$index" }
            ) { rowIndex ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val firstIndex = rowIndex * 2
                    val secondIndex = firstIndex + 1

                    // First card in row
                    CompactSymptomCard(
                        symptomAnalysis = analysisResult.symptomAnalyses[firstIndex],
                        onClick = { selectedSymptom = analysisResult.symptomAnalyses[firstIndex] },
                        modifier = Modifier.weight(1f)
                    )

                    // Second card in row (if exists)
                    if (secondIndex < analysisResult.symptomAnalyses.size) {
                        CompactSymptomCard(
                            symptomAnalysis = analysisResult.symptomAnalyses[secondIndex],
                            onClick = { selectedSymptom = analysisResult.symptomAnalyses[secondIndex] },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Empty spacer to balance grid
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for symptom details
    selectedSymptom?.let { symptom ->
        SymptomDetailsBottomSheet(
            symptomAnalysis = symptom,
            onDismiss = { selectedSymptom = null }
        )
    }
}

@Composable
private fun NoSymptomsEmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.LocalHospital,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Symptom Correlations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "No symptoms have sufficient data for analysis. Continue tracking consistently to identify trigger patterns.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun OverviewStatsCard(analysisResult: AnalysisResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Analysis Overview",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = analysisResult.totalSymptomOccurrences.toString(),
                    label = "Symptoms",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                StatItem(
                    value = analysisResult.totalFoodEntries.toString(),
                    label = "Foods",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                StatItem(
                    value = "${(analysisResult.reliabilityScore * 100).toInt()}%",
                    label = "Reliability",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                StatItem(
                    value = "${analysisResult.observationPeriodDays}d",
                    label = "Period",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun CompactSymptomCard(
    symptomAnalysis: SymptomAnalysis,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (symptomAnalysis.severityLevel) {
                SeverityLevel.HIGH -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                SeverityLevel.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                SeverityLevel.LOW -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Severity indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            when (symptomAnalysis.severityLevel) {
                                SeverityLevel.HIGH -> MaterialTheme.colorScheme.error
                                SeverityLevel.MEDIUM -> MaterialTheme.colorScheme.secondary
                                SeverityLevel.LOW -> MaterialTheme.colorScheme.outline
                            }
                        )
                )

                // Confidence badge
                Text(
                    text = "${(symptomAnalysis.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        symptomAnalysis.confidence >= 0.7 -> MaterialTheme.colorScheme.primary
                        symptomAnalysis.confidence >= 0.5 -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.outline
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Symptom name
            Text(
                text = symptomAnalysis.symptomType,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            // Stats
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${symptomAnalysis.totalOccurrences} times",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Avg ${String.format("%.1f", symptomAnalysis.averageIntensity)}/10",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SymptomDetailsBottomSheet(
    symptomAnalysis: SymptomAnalysis,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Severity indicator
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (symptomAnalysis.severityLevel) {
                                    SeverityLevel.HIGH -> MaterialTheme.colorScheme.error
                                    SeverityLevel.MEDIUM -> MaterialTheme.colorScheme.secondary
                                    SeverityLevel.LOW -> MaterialTheme.colorScheme.outline
                                }
                            )
                    )

                    Text(
                        text = symptomAnalysis.symptomType,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Confidence badge
                AssistChip(
                    onClick = { },
                    label = {
                        Text(
                            text = "${(symptomAnalysis.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when {
                            symptomAnalysis.confidence >= 0.7 -> MaterialTheme.colorScheme.primary
                            symptomAnalysis.confidence >= 0.5 -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
                )
            }

            // Stats summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = symptomAnalysis.totalOccurrences.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Occurrences",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format("%.1f", symptomAnalysis.averageIntensity),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Avg Intensity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Trigger probabilities
            if (symptomAnalysis.triggerProbabilities.isNotEmpty()) {
                Text(
                    text = "Trigger Probabilities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                symptomAnalysis.triggerProbabilities.forEach { trigger ->
                    TriggerProbabilityBar(trigger)
                }
            }

            // Insights
            if (symptomAnalysis.insights.isNotEmpty()) {
                HorizontalDivider()

                Text(
                    text = "Key Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                symptomAnalysis.insights.forEach { insight ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = insight,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SymptomAnalysisCard(
    symptomAnalysis: SymptomAnalysis,
    forceExpanded: Boolean = false
) {
    var manuallyExpanded by remember { mutableStateOf(false) }
    val expanded = forceExpanded || manuallyExpanded

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (symptomAnalysis.severityLevel) {
                SeverityLevel.HIGH -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
                SeverityLevel.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                SeverityLevel.LOW -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with symptom info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { manuallyExpanded = !manuallyExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Severity indicator
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (symptomAnalysis.severityLevel) {
                                    SeverityLevel.HIGH -> MaterialTheme.colorScheme.error
                                    SeverityLevel.MEDIUM -> MaterialTheme.colorScheme.secondary
                                    SeverityLevel.LOW -> MaterialTheme.colorScheme.outline
                                }
                            )
                            .semantics {
                                contentDescription = when (symptomAnalysis.severityLevel) {
                                    SeverityLevel.HIGH -> "High severity symptom"
                                    SeverityLevel.MEDIUM -> "Moderate severity symptom"  
                                    SeverityLevel.LOW -> "Low severity symptom"
                                }
                            }
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = symptomAnalysis.symptomType,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${symptomAnalysis.totalOccurrences} occurrences • Avg intensity ${String.format("%.1f", symptomAnalysis.averageIntensity)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Confidence badge
                AssistChip(
                    onClick = { },
                    label = { 
                        Text(
                            text = "${(symptomAnalysis.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when {
                            symptomAnalysis.confidence >= 0.7 -> MaterialTheme.colorScheme.primary
                            symptomAnalysis.confidence >= 0.5 -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 150, easing = LinearOutSlowInEasing)
                )
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Trigger probabilities
                    if (symptomAnalysis.triggerProbabilities.isNotEmpty()) {
                        Text(
                            text = "Trigger Probabilities",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        
                        symptomAnalysis.triggerProbabilities.forEach { trigger ->
                            TriggerProbabilityBar(trigger)
                        }
                    }

                    // Insights
                    if (symptomAnalysis.insights.isNotEmpty()) {
                        Text(
                            text = "Key Insights",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        
                        symptomAnalysis.insights.forEach { insight ->
                            Row(
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = insight,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TriggerProbabilityBar(trigger: TriggerProbability) {
    var showDetails by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showDetails = true 
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Food category indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(getIBSTriggerCategoryColor(trigger.ibsTriggerCategory))
                        .semantics {
                            contentDescription = "Food category: ${trigger.ibsTriggerCategory.displayName}"
                        }
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = trigger.foodName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = "${trigger.probabilityPercentage}%",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = getProbabilityColor(trigger.probability)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Probability bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                .semantics {
                    contentDescription = "Trigger probability: ${trigger.probabilityPercentage}% for ${trigger.foodName}"
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(trigger.probability.toFloat())
                    .clip(RoundedCornerShape(4.dp))
                    .background(getProbabilityColor(trigger.probability))
            )
        }
        
        // Additional details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${trigger.occurrenceCount} occurrences",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Confidence indicator icon
                Icon(
                    imageVector = when {
                        trigger.confidence >= 0.8 -> Icons.Default.VerifiedUser
                        trigger.confidence >= 0.6 -> Icons.Default.Security
                        trigger.confidence >= 0.4 -> Icons.Default.Info
                        else -> Icons.Default.HelpOutline
                    },
                    contentDescription = "Confidence level",
                    modifier = Modifier.size(12.dp),
                    tint = when {
                        trigger.confidence >= 0.8 -> MaterialTheme.colorScheme.primary
                        trigger.confidence >= 0.6 -> MaterialTheme.colorScheme.secondary
                        trigger.confidence >= 0.4 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.outline
                    }
                )
                
                Text(
                    text = "Confidence: ${(trigger.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        trigger.confidence >= 0.8 -> MaterialTheme.colorScheme.primary
                        trigger.confidence >= 0.6 -> MaterialTheme.colorScheme.secondary
                        trigger.confidence >= 0.4 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = if (trigger.confidence >= 0.6) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
    
    // Trigger Details Dialog
    if (showDetails) {
        TriggerDetailsDialog(
            trigger = trigger,
            onDismiss = { showDetails = false }
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Analysis Failed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
                FilledTonalButton(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun EmptyAnalysisState() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main empty state card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "No Analysis Available",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Start tracking your food and symptoms to unlock powerful insights and pattern detection.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
        
        // Getting started tips card
        GettingStartedTipsCard()
        
        // Minimum data requirements card
        MinimumDataRequirementsCard()
    }
}

@Composable
private fun GettingStartedTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "Getting Started Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val tips = listOf(
                "Log your meals immediately after eating for accurate timing",
                "Record symptoms as soon as you notice them",
                "Include portion sizes and preparation methods",
                "Note your mood and stress levels alongside symptoms",
                "Track consistently for at least 2-3 weeks for patterns"
            )
            
            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun MinimumDataRequirementsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Checklist,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(24.dp)
                )
                
                Text(
                    text = "Minimum Data for Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val requirements = listOf(
                "At least 10 food entries" to "for identifying potential triggers",
                "At least 5 symptom occurrences" to "for correlation analysis", 
                "Minimum 7 days of tracking" to "for temporal patterns",
                "14+ days recommended" to "for reliable insights"
            )
            
            requirements.forEach { (requirement, description) ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    
                    Column {
                        Text(
                            text = requirement,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingTooltipsOverlay(
    currentTooltipIndex: Int,
    onNextTooltip: () -> Unit,
    onPreviousTooltip: () -> Unit,
    onFinishOnboarding: () -> Unit
) {
    val tooltips = listOf(
        OnboardingTooltip(
            title = "Welcome to Analysis!",
            description = "Here you can discover patterns between your food intake and symptoms. Let's walk through the features.",
            targetDescription = "analysis screen overview"
        ),
        OnboardingTooltip(
            title = "Filter Your Data",
            description = "Use these filter controls to focus your analysis on specific time periods, symptom types, or confidence levels.",
            targetDescription = "filter controls"
        ),
        OnboardingTooltip(
            title = "Refresh Analysis",
            description = "Tap here to refresh your analysis with the latest data from your tracking.",
            targetDescription = "refresh button"
        ),
        OnboardingTooltip(
            title = "Get Help Anytime",
            description = "Tap the help button for detailed explanations of analysis features and metrics.",
            targetDescription = "help button"
        ),
        OnboardingTooltip(
            title = "Three Analysis Tabs",
            description = "Switch between Symptoms (correlations), Patterns (behavioral trends), and Insights (plain-language summaries).",
            targetDescription = "tab navigation"
        )
    )
    
    if (currentTooltipIndex < tooltips.size) {
        val currentTooltip = tooltips[currentTooltipIndex]
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Prevent clicks from going through */ }
        ) {
            // Tooltip card
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Header with step indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentTooltip.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Text(
                            text = "${currentTooltipIndex + 1}/${tooltips.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = currentTooltip.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Progress indicator
                    LinearProgressIndicator(
                        progress = (currentTooltipIndex + 1).toFloat() / tooltips.size,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Navigation buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Previous/Skip button
                        if (currentTooltipIndex > 0) {
                            TextButton(onClick = onPreviousTooltip) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Previous")
                            }
                        } else {
                            TextButton(onClick = onFinishOnboarding) {
                                Text("Skip Tour")
                            }
                        }
                        
                        // Next/Finish button
                        if (currentTooltipIndex < tooltips.size - 1) {
                            FilledTonalButton(onClick = onNextTooltip) {
                                Text("Next")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            FilledTonalButton(onClick = onFinishOnboarding) {
                                Text("Get Started!")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class OnboardingTooltip(
    val title: String,
    val description: String,
    val targetDescription: String
)

@Composable
private fun AnalysisHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Analysis Help",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    HelpSection(
                        title = "Understanding Trigger Probabilities",
                        content = "Each food shows the likelihood it triggered your symptoms based on timing and frequency patterns. Higher percentages indicate stronger correlations."
                    )
                }
                
                item {
                    HelpSection(
                        title = "Confidence Levels",
                        content = "Icons and colors indicate how reliable each correlation is:\n• High (80%+): Strong evidence\n• Moderate (50-79%): Emerging pattern\n• Low (<50%): Insufficient data"
                    )
                }
                
                item {
                    HelpSection(
                        title = "Severity Indicators",
                        content = "Color-coded dots show symptom severity:\n• Red: High severity (7-10)\n• Orange: Moderate (4-6)\n• Gray: Mild (1-3)"
                    )
                }
                
                item {
                    HelpSection(
                        title = "Using Filters",
                        content = "Apply filters to focus your analysis:\n• Date ranges for specific periods\n• Severity thresholds to see only significant symptoms\n• Confidence levels to filter reliable correlations"
                    )
                }
                
                item {
                    HelpSection(
                        title = "Tap for Details",
                        content = "Tap any trigger probability bar to see detailed correlation evidence, timing patterns, and supporting data points."
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

@Composable
private fun HelpSection(
    title: String,
    content: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun getIBSTriggerCategoryColor(category: IBSTriggerCategory): Color {
    return when (category) {
        IBSTriggerCategory.DAIRY -> Color(0xFF3F51B5)
        IBSTriggerCategory.GLUTEN -> Color(0xFFFF9800)
        IBSTriggerCategory.FODMAP_HIGH -> Color(0xFFF44336)
        IBSTriggerCategory.CAFFEINE -> Color(0xFF795548)
        IBSTriggerCategory.ALCOHOL -> Color(0xFF9C27B0)
        IBSTriggerCategory.SPICY -> Color(0xFFE91E63)
        IBSTriggerCategory.FATTY -> Color(0xFFFF5722)
        IBSTriggerCategory.ARTIFICIAL_SWEETENERS -> Color(0xFF673AB7)
        IBSTriggerCategory.CITRUS -> Color(0xFFFFEB3B)
        IBSTriggerCategory.BEANS_LEGUMES -> Color(0xFF4CAF50)
        IBSTriggerCategory.OTHER -> Color(0xFF607D8B)
    }
}

private fun getProbabilityColor(probability: Double): Color {
    return when {
        probability >= 0.8 -> Color(0xFFD32F2F) // High - Red
        probability >= 0.6 -> Color(0xFFFF6F00) // Medium-High - Orange  
        probability >= 0.4 -> Color(0xFFFFA000) // Medium - Amber
        probability >= 0.2 -> Color(0xFFFBC02D) // Low-Medium - Yellow
        else -> Color(0xFF388E3C) // Low - Green
    }
}