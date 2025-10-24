# IBS Trigger Attribution - Research Findings

**Date**: 2025-01-24
**Purpose**: Document medical research on identifying food triggers for IBS symptoms

---

## 📚 Research Overview

### Sources Reviewed
1. **PMC6970560**: Food and Symptom Times (FAST) Diary validation study
2. **PubMed 7870442**: Statistical analysis of food and symptom diaries (164 patients)
3. **PMC4822101**: Novel IBS smartphone app pilot study (Stanford)
4. **Commercial Apps**: mySymptoms, MyFoodPal, Bowelle, Cara Care

---

## ⏰ Time Windows

### Clinical Standards
- **Primary finding**: Postprandial symptoms typically occur **within 3 hours** of eating
- **Current practice**: Most studies use 3-4 hour windows
- **Meal challenge tests**: Track symptoms every 30 minutes up to 240 minutes (4 hours)

### Our Current Implementation
- **Current**: 6-hour window
- **Recommendation**: Reduce to **3 hours** for better accuracy

**Rationale**:
- Longer windows increase false positives (unrelated foods get blamed)
- 3 hours aligns with gastrocolic reflex timing
- Matches clinical research standards

---

## 🍽️ Meal-Level Analysis

### Meal Definition (Research Standard)
- **Meal**: All foods consumed within **30-minute window**
- **Snacks**: If within 30 minutes of meal, included as part of meal
- **New meal**: After 30-minute gap

### Example from Research
```
10:00 AM: Coffee + Milk + Toast [MEAL 1]
10:15 AM: Banana              [MEAL 1 - within 30 min]
10:45 AM: Apple               [MEAL 2 - new meal after gap]
```

### Symptom Attribution
When symptom occurs at 1:00 PM (3 hours later):
- Analyze at **meal level**, not individual food level
- Check: Did "Coffee + Milk + Toast" meal trigger?
- Check: Did "Apple" meal trigger?

**Why meal-level?**
- Impossible to isolate which food in a meal caused symptom
- Nutrients interact (e.g., fat slows digestion)
- Clinical research uses this approach

---

## 📊 Statistical Methods Used in Research

### Individual vs Population Analysis
- **Key finding**: Analysis is **per-person**, not population-wide
- What triggers you ≠ what triggers others
- IBS is highly heterogeneous

### Correlation Methods
1. **Spearman correlation coefficient** (most common)
   - Non-parametric (no assumptions about distribution)
   - Measures strength of association
   - Range: -1 (perfect negative) to +1 (perfect positive)

2. **Linear regression** with feature selection
   - Stanford study approach
   - Used for nutrient analysis

3. **Significance testing (P-values)**
   - P ≤ 0.001: Very strong association (⭐⭐⭐)
   - P ≤ 0.05: Strong association (⭐⭐)
   - P > 0.05: Weak/not significant (⭐)

### Handling Collinearity
**Problem**: Some foods/nutrients are highly correlated
- Example: High-fat foods also high in calories
- Can't tell if fat OR calories is the trigger

**Solution (Stanford study)**:
- Calculate pairwise correlations between all nutrients
- If correlation > 0.75, remove one
- Keep nutrient with "highest average correlation"

**Example**:
```
Coffee + Milk meal:
- Caffeine: 180mg
- Lactose: 12g
- Calories: 150

Coffee (alone) meal:
- Caffeine: 180mg
- Lactose: 0g
- Calories: 5

If both Coffee+Milk and Coffee (alone) have 0.85 correlation,
remove one to avoid double-counting.
```

---

## 🎯 Existing App Approaches

### mySymptoms Food Diary (Commercial)
- AI algorithm with configurable lag times
- Statistical correlation analysis
- Shows "suspect foods" with confidence levels
- Proprietary algorithm (details not public)

### MyFoodPal (Research Project)
- **Machine Learning**: XGBoost algorithm
- **Inputs**: Concentration of foods consumed (portion size matters!)
- **Outputs**: Predicted symptom scores
- **Analysis**:
  - Spearman correlation per food group
  - K-means clustering to find associations
  - PCA for dimensionality reduction

### Stanford Study Results
- **73%** of participants had ≥1 strong food-symptom association (P≤0.05)
- Individual regression per participant (personalized)
- 4-hour symptom window
- Feature selection to remove correlated nutrients

---

## ⚠️ Limitations & Challenges

### 1. Causality vs Correlation
- **Cannot prove causation** from diary data alone
- Could be reverse causality:
  - Symptoms → food choices (feel bad → eat comfort food)
  - Not: Food → symptoms

### 2. Confounding Variables Not Accounted For
Research acknowledges these factors are NOT controlled:
- Stress levels
- Time of day (circadian rhythm)
- Day of week
- Medications
- Sleep quality
- Hormonal cycles (women)
- Physical activity

### 3. Dose-Dependent Reactions
- Small portion = no symptom
- Large portion = symptom
- Most analyses don't track portion size
- **MyFoodPal exception**: Uses concentration/amount

### 4. Confirmation Rates
From the 164-patient study:
- Statistical analysis: **75% helpful** as elimination guide
- Re-testing confirmation: **47%** confirmed
- **~50% false positive rate** expected

### 5. Individual Variability
- "IBS and IBD are heterogeneous conditions"
- "Patients often respond differently to the same food"
- 85-90% of IBS patients report food-related symptoms
- But triggers vary person-to-person

---

## 🔬 Research-Based Algorithm (Ideal)

### Step-by-Step Process

```
For each symptom:
  1. Look back 3 hours (symptom window)

  2. Group foods into meals:
     - Foods within 30 min = same meal
     - Calculate meal occurrence count

  3. For each unique meal:
     - Count: Times this meal → symptom (triggered)
     - Count: Times this meal → no symptom (not triggered)
     - Calculate correlation coefficient (Spearman)
     - Calculate P-value (statistical significance)

  4. Remove highly correlated meals:
     - If two meals have correlation > 0.75
     - Keep meal with higher occurrence count

  5. Rank meals:
     - By correlation strength (high to low)
     - Filter: Only show if P ≤ 0.05

  6. Display with confidence levels:
     - ⭐⭐⭐ Very Strong: P ≤ 0.001
     - ⭐⭐ Strong: P ≤ 0.05
     - ⭐ Weak: P > 0.05 (don't show)
```

### Required Data
- **Minimum occurrences**: 5-10 per meal for meaningful P-values
- **Study standard**: 15-20+ entries for reliable statistics
- **Trade-off**: More data = higher confidence, but slower insights

---

## 🤔 Key Questions for Implementation

### 1. Simple Counting vs Statistical Analysis?
- **Simple**: Count triggered vs total occurrences (%)
- **Statistical**: Calculate correlation + P-values
- **Trade-off**: Simplicity vs accuracy

### 2. Minimum Data Requirements?
- **Research standard**: 15-20 entries
- **Practical**: Show analysis even with 3-5 entries?
- **Trade-off**: Early insights vs false positives

### 3. Meal-Level Only or Individual Foods Too?
- **Research**: Meal-level analysis
- **User need**: "Is it the milk or the coffee?"
- **Hybrid**: Show both?

### 4. Portion Size Tracking?
- **Research**: Dose-dependent reactions exist
- **Current**: We don't track portions
- **Future**: Add quantity tracking?

---

## 📝 Citations

1. Barney P, Weisman A, Jarrett M, Levy R, Heitkemper M. "Measuring Diet Intake and Gastrointestinal Symptoms in Irritable Bowel Syndrome: Validation of the Food and Symptom Times Diary." PMC6970560, 2020.

2. "Identification of problem foods using food and symptom diaries." PubMed 7870442, 1995.

3. Dorn SD, Palsson OS, Thiwan SI, et al. "Feasibility and Usability Pilot Study of a Novel Irritable Bowel Syndrome Food and Gastrointestinal Symptom Journal Smartphone App." PMC4822101, 2016.

4. mySymptoms Food Diary. https://www.mysymptoms.net/ (Accessed 2025-01-24)

5. MyFoodPal research project. Devpost. (Accessed 2025-01-24)

---

**Next Steps**: See `PROPOSED_SOLUTIONS.md` for implementation options based on these findings.
