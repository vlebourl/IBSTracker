# Quick Start Guide: Improved Analysis Insights

**Feature**: Symptom-centric trigger analysis with probability-based correlations  
**Date**: 2025-10-26  
**Purpose**: Developer guide for implementing the enhanced Analysis screen

## Overview

This feature transforms the confusing Analysis page into an intuitive, symptom-centric view showing probability-based trigger correlations. Users will see clear insights like "Diarrhea: Coffee (47%), Cheese (85%), Tomato (98%)" with Material Design 3 UI.

## Implementation Phases

### Phase 1: Core Analysis Engine (Days 1-3)

**Priority**: P1 (Critical for MVP)

1. **Create data models** (`data/model/`)
   ```kotlin
   // Key models to implement first
   AnalysisResult.kt
   SymptomAnalysis.kt  
   TriggerProbability.kt
   CorrelationEvidence.kt
   ```

2. **Implement correlation calculation engine** (`data/analysis/`)
   ```kotlin
   // Core algorithm implementation
   CorrelationCalculator.kt  // Weighted correlation formula
   ProbabilityEngine.kt      // Probability calculation
   TriggerAnalyzer.kt        // Main analysis orchestrator
   ```

3. **Set up analysis repository** (`data/repository/`)
   ```kotlin
   AnalysisRepository.kt     // Data access layer
   ```

**Success Criteria**: Basic correlation calculation working with test data

### Phase 2: UI Components (Days 4-6)

**Priority**: P1 (Required for user interaction)

1. **Create core UI components** (`ui/components/analysis/`)
   ```kotlin
   SymptomCard.kt           // Expandable symptom display
   TriggerProbabilityBar.kt // Visual probability indicator
   FilterChips.kt           // Analysis filters
   ```

2. **Update AnalysisScreen** (`ui/screens/`)
   ```kotlin
   AnalysisScreen.kt        // Main screen with new layout
   ```

3. **Create AnalysisViewModel** (`ui/viewmodel/`)
   ```kotlin
   AnalysisViewModel.kt     // State management
   ```

**Success Criteria**: Basic symptom cards display with dummy data

### Phase 3: Food Grouping & Categorization (Days 7-8)

**Priority**: P2 (Important for accuracy)

1. **Implement food grouping utilities** (`utils/`)
   ```kotlin
   FoodGroupMapper.kt       // Automatic categorization
   ```

2. **Add baseline trigger probabilities**
   ```kotlin
   // In FoodCategory enum
   DAIRY("Dairy", 0.65),
   GLUTEN("Gluten", 0.45),
   FODMAP_HIGH("High FODMAP", 0.75),
   // ... etc
   ```

**Success Criteria**: Foods automatically categorized with reasonable accuracy

### Phase 4: Advanced Features (Days 9-10)

**Priority**: P3 (Nice to have)

1. **Pattern detection**
   ```kotlin
   PatternDetectionEngine.kt
   ```

2. **Insight generation**
   ```kotlin
   InsightEngine.kt
   ```

3. **Performance optimization**
   - Caching layer
   - Lazy loading
   - Background calculation

**Success Criteria**: Patterns detected and insights generated

## Key Implementation Details

### Correlation Algorithm

The core correlation formula combines three weighted factors:

```kotlin
fun calculateWeightedCorrelation(
    temporalScore: Double,    // Exponential decay: e^(-0.5 × hours)
    baselineScore: Double,    // Known IBS trigger potential
    frequencyScore: Double    // Historical co-occurrence ratio
): Double {
    return (temporalScore * 0.4) + 
           (baselineScore * 0.3) + 
           (frequencyScore * 0.3)
}
```

### Database Changes

Add new table for correlation caching:

```sql
CREATE TABLE correlation_cache (
    id TEXT PRIMARY KEY,
    food_name TEXT NOT NULL,
    symptom_type TEXT NOT NULL,
    correlation_score REAL,
    confidence_level REAL,
    calculated_at INTEGER,
    data_hash_code INTEGER
);
```

### Material Design 3 Components

Key UI components follow M3 principles:

```kotlin
// Color scheme for probability levels
val severityColors = mapOf(
    0.0..0.4 to Color(0xFF66BB6A),  // Green (low)
    0.4..0.7 to Color(0xFFFFA726), // Orange (medium)  
    0.7..1.0 to MaterialTheme.colorScheme.error // Red (high)
)

// Touch targets meet accessibility requirements
const val MIN_TOUCH_TARGET = 48.dp
```

## Testing Strategy

### Unit Tests
```kotlin
// Test correlation algorithms
CorrelationCalculatorTest.kt
ProbabilityEngineTest.kt
TriggerAnalyzerTest.kt

// Test data models
AnalysisResultTest.kt
TriggerProbabilityTest.kt
```

### UI Tests
```kotlin
// Test screen interactions
AnalysisScreenTest.kt
SymptomCardTest.kt
FilterChipsTest.kt
```

### Integration Tests
```kotlin
// Test end-to-end workflows
AnalysisWorkflowTest.kt
```

## Performance Considerations

### Calculation Optimization
- Cache correlation results in Room database
- Use background threads for heavy calculations
- Implement incremental updates when new data added

### UI Performance
- Use `LazyColumn` with stable keys for symptom list
- Implement content type optimization for Compose
- Use `remember` for expensive UI calculations

### Memory Management
- Limit analysis to configurable time periods (default 90 days)
- Paginate large result sets
- Clear caches periodically

## Common Integration Points

### Existing Database
The feature integrates with existing Room entities:
- `FoodItem` → Source for food consumption data
- `Symptom` → Source for symptom occurrence data

### Existing Navigation
Add analysis screen to existing nav graph:
```kotlin
composable("analysis") {
    AnalysisScreen(...)
}
```

### Existing Dependencies
Leverages current tech stack:
- Room for data persistence
- Jetpack Compose for UI
- Material3 for design system
- Kotlin Coroutines for async operations

## Validation Checklist

Before considering implementation complete:

- [ ] Correlation calculations produce reasonable results
- [ ] UI components follow Material Design 3 guidelines
- [ ] Touch targets meet accessibility requirements (48dp minimum)
- [ ] Analysis loads in <2 seconds for 90 days of data
- [ ] Food categorization achieves >80% accuracy
- [ ] All unit tests pass
- [ ] UI tests verify key user flows
- [ ] Performance meets success criteria

## Deployment Notes

### Feature Flags
Consider feature flag for gradual rollout:
```kotlin
if (FeatureFlags.IMPROVED_ANALYSIS_ENABLED) {
    // Use new analysis screen
} else {
    // Fall back to current implementation
}
```

### Migration Strategy
- New analysis runs alongside existing system initially
- Users can switch between old/new views during transition
- Remove old implementation after validation period

### Monitoring
Track key metrics:
- Analysis calculation time
- User engagement with new insights
- Error rates in correlation engine
- Performance on various device types

This quick start guide provides the roadmap for implementing sophisticated symptom-trigger analysis that transforms complex data into clear, actionable health insights for IBS Tracker users.