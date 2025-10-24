# IBS Trigger Attribution - Proposed Solutions

**Date**: 2025-01-24
**Purpose**: Document practical implementation approaches based on research findings

---

## 🎯 Design Goals

Based on user requirements:

1. ✅ **Start with Phase 1** - Quick, research-informed improvements
2. ✅ **Personal use** - User familiar with statistical concepts
3. ✅ **Show early insights** - Don't require 15-20 data points
4. ✅ **Balance speed & accuracy** - ASAP implementation but scientifically sound

---

## 📋 Phase 1: Research-Informed Simple Approach

### Changes from Current Implementation

#### 1. **Reduce Symptom Window: 6h → 3h** ⭐ CRITICAL
**Rationale**: Clinical research shows 3-hour postprandial window
```kotlin
// OLD
private const val SYMPTOM_WINDOW_HOURS = 6

// NEW
private const val SYMPTOM_WINDOW_HOURS = 3
```

**Impact**:
- ✅ Fewer false positives
- ✅ More accurate attribution
- ✅ Matches clinical standards

---

#### 2. **Group Foods into Meals** ⭐ NEW CONCEPT

**Meal Definition**:
- Foods eaten within **30 minutes** = same meal
- Track meals as atomic units

**Example**:
```
10:00 AM: Coffee, Milk, Toast → MEAL_1
10:15 AM: Banana              → MEAL_1 (within 30 min)
10:45 AM: Apple               → MEAL_2 (new meal)
```

**Implementation**:
```kotlin
data class Meal(
    val foods: List<FoodItem>,
    val timestamp: Date, // timestamp of first food
    val windowEnd: Date  // timestamp + 30 minutes
)

fun groupIntoMeals(foods: List<FoodItem>): List<Meal> {
    // Sort by timestamp
    // Group consecutive foods within 30 min
}
```

---

#### 3. **Dual-Track Analysis: Meals + Individual Foods**

**Why Both?**
- **Research**: Says analyze meals
- **User need**: "Is it coffee or milk?"

**Solution**: Show both with context

**Display**:
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📌 Meal Triggers (High Confidence)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
☕ Coffee + 🥛 Milk + 🥖 Toast
   8/10 meals triggered (80%)
   Symptoms: Bloating (5x), Pain (3x)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔍 Individual Foods (Lower Confidence*)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
☕ Coffee (any meal containing it)
   12/15 times triggered (80%)
   ⚠️ Often eaten with milk (9/15 times)

🥛 Milk (any meal containing it)
   10/12 times triggered (83%)
   ⚠️ Often eaten with coffee (9/12 times)

🥖 Toast (any meal containing it)
   4/8 times triggered (50%)

*Individual food attribution is less reliable
when foods are eaten together.
```

**Key Feature**: Show **co-occurrence warnings**
- "Often eaten with X" alerts user to possible confounding

---

#### 4. **Minimum Occurrence Filter**

**Research**: 15-20 entries for reliable statistics
**Our Approach**: Show warnings for low data

```
☕ Coffee: 2/3 times (67%)
   ⚠️ Low confidence - only 3 occurrences
   Track more to improve accuracy

🥛 Milk: 8/10 times (80%)
   ✅ Good confidence - 10 occurrences
```

**Implementation**:
```kotlin
data class TriggerConfidence(
    val level: ConfidenceLevel,
    val message: String
)

enum class ConfidenceLevel {
    VERY_LOW,   // 1-2 occurrences
    LOW,        // 3-4 occurrences
    MODERATE,   // 5-9 occurrences
    GOOD,       // 10-14 occurrences
    HIGH        // 15+ occurrences
}

fun getConfidence(occurrences: Int): TriggerConfidence {
    return when (occurrences) {
        in 1..2 -> TriggerConfidence(
            VERY_LOW,
            "Very low confidence - need more data"
        )
        in 3..4 -> TriggerConfidence(
            LOW,
            "Low confidence - only $occurrences occurrences"
        )
        // ... etc
    }
}
```

---

#### 5. **Show Isolation Status**

Track if food was eaten **alone** (no other foods in meal)

```
☕ Coffee:
   Solo: 2/3 times (67%) ← High confidence
   In meals: 6/9 times (67%) ← Lower confidence
```

**Why useful**:
- Solo triggers = clear cause-effect
- Meal triggers = possible combination effect

---

### Phase 1 Summary

**What Changes**:
1. ✅ 3-hour window (not 6)
2. ✅ Meal grouping (30-min window)
3. ✅ Dual display (meals + foods)
4. ✅ Co-occurrence warnings
5. ✅ Confidence levels based on data points
6. ✅ Isolation tracking

**What Stays Same**:
- Simple percentage calculation (triggered/total)
- No statistical tests (P-values)
- No correlation coefficients
- Easy to understand

**Benefits**:
- ✅ Quick to implement
- ✅ Research-informed
- ✅ More accurate than current
- ✅ Shows insights early (don't need 15+ entries)
- ✅ Transparent about limitations

---

## 📊 Phase 2: Statistical Enhancement (Future)

**When to Consider**: After Phase 1 deployed and more data collected

### Additional Features

#### 1. **Spearman Correlation**
Calculate correlation coefficient for each trigger:
```
☕ Coffee:
   Correlation: ρ = 0.82
   Significance: P < 0.01 ⭐⭐⭐
   12/15 times (80%)
```

**Requirements**:
- Add statistics library (Apache Commons Math)
- Minimum 10 data points per food
- More complex to explain to users

---

#### 2. **Lag Time Analysis**
Track **when** symptoms occur after meals:
```
☕ Coffee:
   Average lag: 2.3 hours
   Range: 1.5 - 2.8 hours
   Pattern: Consistent timing
```

**Benefits**:
- Helps identify triggers with delayed reactions
- Can adjust time windows per food

---

#### 3. **Portion Size Impact**
Track if amount eaten affects symptoms:
```
☕ Coffee:
   Small (1 cup): 2/8 times (25%)
   Medium (2 cups): 4/6 times (67%)
   Large (3+ cups): 6/6 times (100%)

   ⚠️ Dose-dependent trigger!
```

**Requirements**:
- Add quantity tracking to FoodItem
- User must log portions consistently

---

#### 4. **Combination Detection Algorithm**
Automatically identify problematic combinations:
```
🔍 Detected Problem Combinations:
   ☕ Coffee + 🥛 Milk: 9/9 times (100%)
   But individually:
   - Coffee alone: 2/6 times (33%)
   - Milk alone: 1/5 times (20%)

   → Synergistic effect detected
```

**Algorithm**:
```kotlin
// For each pair of foods that appear together
if (combo.triggerRate > food1.soloRate + threshold
    && combo.triggerRate > food2.soloRate + threshold) {
    // Synergistic effect
}
```

---

#### 5. **Confounding Variables**
Track and adjust for:
- Time of day
- Day of week
- Stress level (user-reported)
- Sleep quality
- Menstrual cycle

**Advanced**: Multi-variate regression
```kotlin
SymptomScore =
    β₁ * Coffee +
    β₂ * Milk +
    β₃ * StressLevel +
    β₄ * TimeOfDay +
    ε (error term)
```

---

## 🎨 UI/UX Improvements

### Visual Hierarchy

**Priority 1: Meals with High Confidence**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
⚠️ CLEAR TRIGGERS (High Confidence)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
☕ Coffee + 🥛 Milk
   8/10 meals (80%) ✅ Good confidence
   Eaten together 8 times
   Symptoms: Bloating (5x), Pain (3x)
```

**Priority 2: Individual Foods with Context**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🔍 INDIVIDUAL FOOD ANALYSIS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
☕ Coffee: 12/15 (80%)
   Solo: 2/3 (67%)
   In meals: 10/12 (83%)
   Often with: Milk (9x), Toast (6x)
```

**Priority 3: Needs More Data**
```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📊 TRACK MORE FOR INSIGHTS
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
🍎 Apple: 1/2 (50%)
   ⚠️ Only 2 occurrences - need more data
```

---

### Expandable Details

```
☕ Coffee: 12/15 (80%) [▼]

[Expanded:]
☕ Coffee
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
Trigger Rate: 12/15 times (80%)
Confidence: ✅ Good (15 occurrences)

Solo Analysis:
  2/3 times (67%)

Meal Analysis:
  10/12 times (83%)
  Often with: Milk (9x), Toast (6x)

Symptom Breakdown:
  Bloating: 7 times
  Pain: 3 times
  Diarrhea: 2 times

Time Pattern:
  Average lag: 2.1 hours
  Range: 1.5 - 2.8 hours

Last triggered: 2 days ago
```

---

## 🔄 Migration Path

### Current State
```kotlin
// Only tracks individual foods
// 6-hour window
// No meal concept
```

### Phase 1 Changes
```kotlin
// Database: No schema changes needed!
// Just analysis logic changes:

1. Change constant: SYMPTOM_WINDOW_HOURS = 3
2. Add meal grouping function
3. Update AnalyticsEngine with dual tracking
4. Update UI to show meals + foods
```

### Phase 2 Changes (Future)
```kotlin
// Database schema additions:
// - FoodItem.quantity: Float?
// - DailyLog.stressLevel: Int?
// - DailyLog.sleepQuality: Int?

// Analysis additions:
// - Statistical library
// - Correlation calculations
// - Multi-variate regression
```

---

## 📝 Implementation Checklist - Phase 1

### Analytics Engine
- [ ] Change `SYMPTOM_WINDOW_HOURS` from 6 to 3
- [ ] Add `Meal` data class
- [ ] Implement `groupIntoMeals()` function
- [ ] Add `analyzeMealTriggers()` function
- [ ] Update `analyzeFoodItemTriggers()` to track:
  - [ ] Solo occurrences
  - [ ] Meal occurrences
  - [ ] Co-occurrence tracking
- [ ] Add confidence level calculation
- [ ] Update `InsightSummary` to include:
  - [ ] `mealTriggers: List<MealTrigger>`
  - [ ] Update `FoodItemTrigger` with isolation data

### Data Models
- [ ] Create `Meal` data class
- [ ] Create `MealTrigger` data class
- [ ] Update `FoodItemTrigger` with:
  - [ ] `soloOccurrences: Int`
  - [ ] `soloTriggered: Int`
  - [ ] `mealOccurrences: Int`
  - [ ] `mealTriggered: Int`
  - [ ] `coOccurrences: Map<String, Int>`
  - [ ] `confidence: ConfidenceLevel`

### UI Components
- [ ] Add `MealTriggerCard` component
- [ ] Update `FoodTriggerCard` to show:
  - [ ] Solo vs meal breakdown
  - [ ] Co-occurrence warnings
  - [ ] Confidence indicators
- [ ] Add expandable detail view
- [ ] Add "Low confidence" warnings
- [ ] Visual hierarchy (meals first, foods second)

### Testing
- [ ] Test meal grouping logic
- [ ] Test 3-hour window
- [ ] Test with low data (1-2 entries)
- [ ] Test with medium data (5-10 entries)
- [ ] Test with high data (15+ entries)
- [ ] Verify co-occurrence detection

---

## 💡 Quick Wins (Immediate Impact)

These require minimal changes but have high impact:

1. **Change window to 3 hours** - 1 line of code
2. **Add confidence warnings** - 10 lines of code
3. **Show co-occurrence** - 20 lines of code

Total implementation: ~30 lines of code
Impact: Significantly more accurate

---

## 🤔 Open Questions

1. **Meal grouping edge cases**:
   - What if someone eats continuously (snacking)?
   - Define "meal" more precisely?

2. **UI space constraints**:
   - Too much information overwhelming?
   - Which details collapse/expand?

3. **Confidence thresholds**:
   - 3 occurrences = "Low" or "Very Low"?
   - Adjust based on user feedback?

4. **IBS attributes**:
   - Keep current attribute analysis?
   - Integrate with meal analysis?

---

## 📋 Next Steps

1. **Review**: User reviews research findings + proposed solutions
2. **Decide**: Confirm Phase 1 scope
3. **Implement**: Start with Phase 1 changes
4. **Test**: Validate with real data
5. **Iterate**: Collect feedback, adjust confidence thresholds
6. **Plan Phase 2**: When ready for statistical enhancements

---

**See**: `RESEARCH_FINDINGS.md` for detailed research background
