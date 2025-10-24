## Completed ✅

### v1.9.1 - Deprecation Fixes & Quick-Add Restoration
* ~~Fix all deprecation warnings (30 warnings across 7 files)~~ **[Completed - All external warnings eliminated, 24.5% build time improvement]**
* ~~Quick-add row for frequently used foods (1x4 layout)~~ **[Completed - Regression fixed with Migration 9→10 for historical data backfill]**

### v1.9.0 - Smart Food Categorization System
* ~~Reorganize food quick add categorization~~ **[Completed - 12 actual food categories with hidden IBS attributes for analysis]**
* ~~Pre-populate common foods database~~ **[Completed - 72 French-focused foods with accurate IBS attributes]**

## TODO

### High Priority
* **Fix colors in food selection** - Once a category is selected, colors need improvement
* **Verify "potential triggers" calculation** - Check what the "16/20 times" percentage represents and if it's correct

### Feature Enhancements
* **Add medication tracking** - Add medication part in the "symptoms" tab
* **Body weight history** - Track weight over time and add graph in analysis page
* **Refactor symptom page** - Match the style and functionalities of the food page
* **Swipe gestures for edit/delete** - Implement swipe left to edit, swipe right to delete with confirmation (like email apps)

### UI/UX Improvements
* **Global Material Design alignment** - Run analysis and create plan to realign with latest Material Design 3 recommendations
* **Color harmonization** - Harmonize colors across food, categories, quick-adds, symptoms, analysis (e.g., dairy brightness vs fruits)
* **Category ordering** - Reorder categories by frequency of food entries (most frequent first, without counter)

### Analytics Improvements
* **Granular trigger analysis** - In "potential triggers", show actual food items instead of categories
  - Include IBS attributes (e.g., "Coffee 80% - Acidic, FODMAP HIGH, Caffeine")
  - Make correlations more actionable with specific food data