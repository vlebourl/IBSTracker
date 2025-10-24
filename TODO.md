## Completed ✅

### v1.9.1 - Deprecation Fixes & Quick-Add Restoration
* ~~Fix all deprecation warnings (30 warnings across 7 files)~~ **[Completed - All external warnings eliminated, 24.5% build time improvement]**
* ~~Quick-add row for frequently used foods (1x4 layout)~~ **[Completed - Regression fixed with Migration 9→10 for historical data backfill]**

### v1.9.0 - Smart Food Categorization System
* ~~Reorganize food quick add categorization~~ **[Completed - 12 actual food categories with hidden IBS attributes for analysis]**
* ~~Pre-populate common foods database~~ **[Completed - 72 French-focused foods with accurate IBS attributes]**

---

## TODO

### 🔴 Critical / Bugs
**Priority**: Fix these first
* **Fix colors in food selection** - Once a category is selected, colors need improvement for readability
* **Verify "potential triggers" calculation** - Investigate what "16/20 times" percentage represents and ensure accuracy

### ⭐ New Features
**Priority**: Major functionality additions
* **Add medication tracking** - New tracking category with medication name, dosage, time, and integration with symptom correlation
  - Add to "Symptoms" tab or create separate "Medications" section
  - Pre-populate common IBS medications
  - Track medication-symptom correlations
* **Body weight history** - Track weight changes over time
  - Add weight entry UI
  - Display trend graph in analysis page
  - Correlate with symptom patterns

### 🎨 UI/UX Polish
**Priority**: Visual consistency and user experience
* **Color harmonization** - Ensure consistent color scheme across all screens
  - Fix dairy brightness vs fruits contrast
  - Harmonize food, categories, quick-adds, symptoms, analysis colors
  - Apply Material Design 3 color palette consistently
* **Global Material Design 3 alignment** - Comprehensive UI audit
  - Analyze current UI against MD3 guidelines
  - Create design system specification
  - Apply consistently across all screens
* **Category ordering** - Dynamically reorder food categories by usage frequency
  - Most frequently used categories first
  - No visible counter (backend sorting only)

### 📊 Analytics & Insights
**Priority**: Better health correlations and actionable data
* **Granular trigger analysis** - Replace category-level with food-item-level triggers
  - Show actual foods instead of categories (e.g., "Coffee" not "Beverages")
  - Display IBS attributes for context (e.g., "Coffee 80% - Acidic, FODMAP HIGH, Caffeine")
  - Make correlations more actionable with specific recommendations
  - Requires "verify triggers calculation" to be completed first

### 🔧 Technical Improvements
**Priority**: Code quality and consistency
* **Refactor symptom page** - Modernize to match food page architecture
  - Apply same UI patterns and components
  - Add search functionality
  - Implement Material Design 3 styling
  - Improve edit/delete workflows
* **Swipe gestures for edit/delete** - Implement intuitive gestures across all lists
  - Swipe left reveals edit action
  - Swipe right reveals delete with confirmation
  - Apply to food items, symptoms, and future medication lists
  - Follow email app patterns for familiarity

---

## 📋 Recommended Approach

### Phase 1: Polish Current Release (v1.9.2) - 1-2 days
1. Fix colors in food selection
2. Verify potential triggers calculation
3. Color harmonization

### Phase 2: Major Feature (v1.10.0) - 3-5 days
Choose one:
- **Option A**: Medication tracking (most requested)
- **Option B**: Analytics enhancement (leverage v1.9.0 work)

### Phase 3: UI/UX Overhaul (v1.11.0) - 5-7 days
1. Global Material Design 3 alignment
2. Refactor symptom page
3. Implement swipe gestures
4. Category ordering by frequency

### Phase 4: Advanced Features (v1.12.0+) - Ongoing
1. Body weight history
2. Additional tracking categories
3. Export/import improvements