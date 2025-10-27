# Research: Improved Analysis Insights

**Feature**: Symptom-centric trigger analysis with probability-based correlations  
**Date**: 2025-10-26  
**Purpose**: Research technical approaches for implementing weighted correlation analysis and Material Design 3 UI patterns

## Key Research Areas

### 1. Correlation Analysis Algorithms

**Decision**: Implement weighted correlation combining temporal proximity, baseline IBS trigger probabilities, and historical frequency patterns

**Rationale**: 
- Single-factor correlations (frequency only) are insufficient for accurate trigger identification
- Medical research shows temporal proximity is critical (most IBS reactions occur within 2-8 hours)
- Baseline trigger probabilities provide domain knowledge (dairy=65%, coffee=55%, etc.)
- Combined weighting provides more accurate and actionable insights

**Algorithm Implementation**:
```kotlin
// Core formula: Combined Score = (0.4 × Temporal) + (0.3 × Baseline) + (0.3 × Frequency)
fun calculateWeightedCorrelation(
    temporalProximityScore: Double,  // Exponential decay: e^(-0.5 × hours)
    baselineTriggerProbability: Double, // Known IBS trigger potential
    historicalFrequency: Double      // Occurrence ratio
): Double
```

**Alternatives considered**:
- Pure frequency analysis: Too simplistic, ignores timing and food types
- Pearson correlation: Inappropriate for sparse, temporal health data
- Machine learning models: Overkill for local app, requires large datasets

### 2. Food Grouping Strategies

**Decision**: Hybrid approach using Levenshtein distance similarity + keyword-based categorization

**Rationale**:
- Users enter similar foods with variations ("coffee", "Coffee", "coffee with milk")
- Medical categories matter more than exact names (all dairy products share trigger characteristics)
- Automatic grouping reduces manual categorization effort

**Implementation Approach**:
- Primary: Keyword matching for medical categories (dairy, gluten, FODMAP, caffeine)
- Secondary: String similarity for variations (70% threshold)
- Fallback: "Other" category with individual analysis

**Alternatives considered**:
- Manual categorization: Too burdensome for users
- ML-based clustering: Computationally expensive, requires training data
- Exact string matching: Misses variations and similar foods

### 3. Probability Calculation

**Decision**: Bayesian-inspired approach with confidence intervals for statistical reliability

**Rationale**:
- Users need intuitive percentages, not correlation coefficients
- Small sample sizes common in health tracking (sparse symptoms)
- Confidence levels help users understand reliability

**Calculation Method**:
```kotlin
// Base probability from weighted correlation (0-1)
val baseProbability = weightedCorrelation
// Adjust for symptom intensity (high intensity = higher impact)
val intensityMultiplier = 1.0 + (avgSymptomIntensity / 10.0) * 0.5
// Final percentage with confidence weighting
val displayProbability = (baseProbability * intensityMultiplier * confidence * 100).toInt()
```

**Alternatives considered**:
- Direct correlation coefficients: Not intuitive for non-technical users
- Simple frequency ratios: Ignores temporal and baseline factors
- Complex statistical models: Too complex for mobile implementation

### 4. Statistical Significance Thresholds

**Decision**: Multi-tier reliability system with minimum occurrence requirements

**Rationale**:
- Prevents misleading correlations from insufficient data
- Provides clear guidance on reliability levels
- Balances showing insights vs. avoiding false positives

**Threshold Implementation**:
- Hide: <3 occurrences OR <14 observation days
- Low Confidence: 3-4 occurrences OR 14-21 days
- Medium: 5-9 occurrences OR 21-30 days  
- High: 10+ occurrences AND 30+ days

**Alternatives considered**:
- Fixed minimum thresholds: Too rigid for varying user patterns
- No thresholds: Risk of misleading single-occurrence correlations
- Complex statistical tests: Overkill for consumer health app

### 5. Material Design 3 UI Patterns

**Decision**: Card-based expandable layout with color-coded probability indicators

**Rationale**:
- Cards provide clear visual grouping for each symptom
- Expandable design reduces cognitive load while providing detail access
- Color coding enables quick pattern recognition
- Follows 2025 Material Design 3 accessibility guidelines

**Key Components**:
- Expandable cards with spring animations (dampingRatio: MediumBouncy)
- Linear progress bars for probability visualization
- Color scheme: Red (>70%), Orange (40-70%), Green (<40%)
- Filter chips for date range and severity selection
- 48dp minimum touch targets for accessibility

**Typography Hierarchy**:
- titleLarge: Symptom names (primary information)
- bodyLarge: Food trigger names (secondary information)  
- bodyMedium: Probability percentages (tertiary information)
- bodySmall: Supporting context (occurrence counts, timeframes)

**Alternatives considered**:
- List-based layout: Less visual hierarchy, harder to scan
- Chart-based visualization: Too complex for mobile screens
- Table format: Not suitable for touch interfaces

### 6. Performance Optimization

**Decision**: Local calculation with intelligent caching and lazy loading

**Rationale**:
- Health data must remain private (local processing)
- Correlation calculation is computationally light for typical datasets
- Caching prevents recalculation on UI interactions

**Implementation Strategy**:
- Calculate correlations on background thread when data changes
- Cache results in Room database with timestamp
- Use LazyColumn with stable keys for efficient list rendering
- Implement content type optimization for Compose recomposition

**Alternatives considered**:
- Real-time calculation: Too slow for responsive UI
- Cloud processing: Privacy concerns, requires internet
- Pre-computed static analysis: Doesn't adapt to new data

### 7. Time Window Analysis

**Decision**: 8-hour window with exponential time decay weighting

**Rationale**:
- Medical literature suggests most IBS food reactions occur within 2-8 hours
- Exponential decay reflects reality that closer timing = higher causation likelihood
- 8-hour window captures delayed reactions while limiting false correlations

**Decay Function**: `weight = e^(-0.5 × hours_elapsed)`
- 1 hour: weight = 0.61 (strong correlation)
- 4 hours: weight = 0.14 (moderate correlation)  
- 8 hours: weight = 0.02 (weak correlation)

**Alternatives considered**:
- Fixed time windows: Doesn't reflect varying reaction speeds
- 24-hour window: Too many unrelated foods included
- User-configurable window: Adds complexity without clear benefit

## Technical Implementation Decisions

### Database Schema Extensions
- New table: `food_correlations` for cached correlation results
- Indexes on `correlation_score DESC` and `food_name` for performance
- Timestamp tracking for cache invalidation

### Architecture Additions
- `AnalysisRepository`: Centralized correlation calculation and caching
- `ProbabilityEngine`: Core probability calculation algorithms
- `FoodGroupMapper`: Automatic food categorization logic

### Testing Strategy
- Unit tests for correlation algorithms with known datasets
- UI tests for filter interactions and accessibility
- Performance tests for large datasets (1000+ food entries)

## Validation Approach

### Algorithm Validation
- Test with synthetic datasets with known correlations
- Compare results against expected medical patterns
- User testing with real health tracking data

### UI/UX Validation  
- Accessibility testing with screen readers and reduced motion
- Performance testing on mid-range Android devices
- User comprehension testing for probability displays

This research provides the foundation for implementing sophisticated yet user-friendly symptom-trigger analysis that transforms confusing metrics into clear, actionable health insights.