# Analysis Feature Documentation

## Overview

The Improved Analysis Insights feature provides users with a symptom-centric view of food-symptom correlations, displaying probability-based trigger analysis with confidence scoring and detailed explanations.

## Architecture

### Core Components

#### Data Analysis Engine
- **CorrelationCalculator**: Implements weighted correlation algorithm (40% temporal + 30% baseline + 30% frequency)
- **ProbabilityEngine**: Generates symptom analysis with confidence scoring and recommendation levels
- **TriggerAnalyzer**: Main orchestrator for analysis workflow with memory optimization for large datasets

#### Data Models
- **AnalysisResult**: Container for complete analysis with metadata
- **SymptomAnalysis**: Symptom-specific analysis with trigger probabilities and insights
- **TriggerProbability**: Food-symptom correlation data with confidence metrics
- **CorrelationEvidence**: Individual correlation event with temporal and intensity data
- **IBSTriggerCategory**: Enum with 10 food categories and baseline probabilities

#### UI Components
- **AnalysisScreen**: Main analysis interface with Material Design 3 components
- **TriggerDetailsDialog**: Detailed correlation explanation dialog (User Story 2)
- **CorrelationMetrics**: Visual correlation metrics with charts (User Story 2)
- **InsightText**: Smart insights with expandable content (User Story 2)

#### Repository Layer
- **AnalysisRepositoryImpl**: Caching and data orchestration with incremental updates
- **CorrelationCache**: Performance optimization through Room database caching

### Algorithm Details

#### Weighted Correlation Formula
```
probability = (temporal_score * 0.4) + (baseline_score * 0.3) + (frequency_score * 0.3)
```

#### Temporal Scoring
- 8-hour analysis window with exponential decay
- Recent correlations weighted higher
- `temporal_weight = exp(-hours_lag / max_hours)`

#### Confidence Calculation
- Based on sample size and consistency
- `confidence = (sample_size_weight + consistency_weight) / 2`
- Minimum 3 occurrences for reliable patterns

#### Recommendation Levels
- **HIGH**: confidence ≥ 0.7, occurrences ≥ 5
- **MEDIUM**: confidence ≥ 0.5, occurrences ≥ 3
- **LOW_CONFIDENCE**: confidence ≥ 0.2
- **HIDE**: insufficient data

## Implementation Status

### Completed Features (MVP - User Story 1)
✅ **Core Analysis Engine**
- Weighted correlation algorithm
- Temporal proximity calculation
- Baseline trigger probability lookup
- Frequency correlation calculation
- Probability percentage calculation
- Confidence scoring

✅ **UI Implementation**
- Symptom-centric analysis display
- Probability percentage visualization
- Color-coded severity indicators
- Material Design 3 integration
- Expandable symptom cards
- Trigger probability bars

✅ **Performance Optimizations**
- Result caching (30-minute TTL)
- Background computation with coroutines
- Lazy loading for large datasets
- Memory optimization for 5000+ data points
- Incremental cache updates

### Completed Features (User Story 2)
✅ **Enhanced Correlation Display**
- TriggerDetailsDialog with comprehensive metrics
- CorrelationMetrics with circular charts
- InsightText with smart explanations
- Confidence indicators and breakdowns

### Testing Coverage
✅ **Unit Tests**
- CorrelationCalculatorTest (core algorithm validation)
- ProbabilityEngineTest (symptom analysis generation)
- TriggerAnalyzerTest (integration testing)
- FoodGroupMapperTest (food categorization)

✅ **UI Tests**
- AnalysisScreenTest (user interface validation)

## Usage Examples

### Basic Analysis
```kotlin
val analysisResult = triggerAnalyzer.generateAnalysisResult(
    timeWindow = AnalysisTimeWindow(
        startDate = LocalDate.now().minusDays(30),
        endDate = LocalDate.now(),
        windowSizeHours = 8,
        minimumOccurrences = 3,
        minimumObservationDays = 14
    ),
    filters = AnalysisFilters()
)
```

### Filtered Analysis
```kotlin
val filters = AnalysisFilters(
    severityThreshold = 7,
    symptomTypes = setOf("Diarrhea", "Nausea"),
    minimumConfidence = 0.5
)
```

### UI Integration
```kotlin
AnalysisScreen(
    analysisResult = analysisResult,
    isLoading = false,
    errorMessage = null,
    onRefresh = { viewModel.refreshAnalysis() },
    onTimeWindowChange = { viewModel.updateTimeWindow(it) },
    onFilterChange = { viewModel.updateFilters(it) }
)
```

## Food Categories

The system recognizes 10 IBS trigger categories:

1. **DAIRY** (baseline: 0.7) - milk, cheese, yogurt
2. **GLUTEN** (baseline: 0.65) - wheat, bread, pasta
3. **FODMAP_HIGH** (baseline: 0.6) - onions, garlic, beans
4. **CAFFEINE** (baseline: 0.55) - coffee, tea, energy drinks
5. **ALCOHOL** (baseline: 0.6) - beer, wine, spirits
6. **SPICY** (baseline: 0.5) - hot peppers, curry, spices
7. **FATTY** (baseline: 0.45) - fried foods, nuts, oils
8. **ARTIFICIAL_SWEETENERS** (baseline: 0.4) - diet products, sugar-free
9. **CITRUS** (baseline: 0.35) - oranges, lemons, acidic fruits
10. **BEANS_LEGUMES** (baseline: 0.6) - lentils, chickpeas, peas

## Performance Characteristics

- **Analysis Generation**: <2 seconds for 90 days of data
- **Memory Usage**: Optimized for 5000+ entries with batch processing
- **Cache Performance**: 30-minute TTL with incremental updates
- **UI Responsiveness**: 60fps smooth scrolling maintained
- **Database Operations**: Room SQLite with correlation cache table

## Future Enhancements

### User Story 3 - Filtering System (Planned)
- Date range picker dialog
- Severity threshold slider
- Symptom type filters
- Real-time analysis updates

### User Story 4 - Natural Language Insights (Planned)
- Pattern detection engine
- Plain-language summaries
- Actionable recommendations
- Onboarding tooltips

### Additional Polish (Planned)
- Accessibility labels and descriptions
- Haptic feedback for interactions
- Animation optimizations
- Advanced error handling

## Configuration

### Analysis Parameters
```kotlin
companion object {
    private const val MAX_TIME_LAG_HOURS = 8
    private const val MIN_CONFIDENCE_THRESHOLD = 0.2
    private const val MIN_OCCURRENCES_FOR_HIGH_CONFIDENCE = 5
    private const val MIN_OCCURRENCES_FOR_MEDIUM_CONFIDENCE = 3
    private const val CACHE_MAX_AGE_MINUTES = 30
    private const val MEMORY_OPTIMIZATION_THRESHOLD = 5000
}
```

### Color Coding
- **Red (High Probability)**: ≥70% correlation
- **Orange (Medium Probability)**: 40-69% correlation
- **Green (Low Probability)**: <40% correlation

## Troubleshooting

### No Correlations Found
- Ensure minimum 3 symptom occurrences
- Verify foods logged within 8-hour window
- Check severity threshold settings
- Extend observation period to 14+ days

### Low Confidence Scores
- Increase data collection period
- Log more detailed food quantities
- Ensure consistent symptom reporting
- Review time window settings

### Performance Issues
- Clear correlation cache if stale
- Reduce analysis time window
- Apply symptom type filters
- Contact support for datasets >10,000 entries

## Integration Notes

### Dependencies
- Jetpack Compose for UI
- Room Database for persistence
- Kotlin Coroutines for background processing
- Material Design 3 for theming

### Database Schema
```sql
CREATE TABLE correlation_cache (
    id TEXT PRIMARY KEY,
    food_name TEXT NOT NULL,
    symptom_type TEXT NOT NULL,
    correlation_score REAL NOT NULL,
    confidence REAL NOT NULL,
    calculated_at INTEGER NOT NULL,
    data_hash_code INTEGER NOT NULL,
    filters TEXT NOT NULL,
    time_window TEXT NOT NULL,
    is_valid INTEGER NOT NULL DEFAULT 1
);
```

### API Surface
- `AnalysisRepository.generateAnalysis(timeWindow, filters): AnalysisResult`
- `AnalysisRepository.getCachedAnalysis(timeWindow, filters): AnalysisResult?`
- `AnalysisRepository.invalidateCache(since: Instant)`
- `AnalysisRepository.observeAnalysisResults(): Flow<AnalysisResult?>`

---

*Last updated: 2025-10-26*
*Implementation version: 1.0 (MVP + User Story 2 Core Components)*