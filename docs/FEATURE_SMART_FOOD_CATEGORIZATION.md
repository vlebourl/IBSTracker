# Smart Food Categorization System

**Feature Branch:** `feature/smart-food-categorization`
**Target Version:** 1.9.0
**Status:** 🚧 In Development

## 📋 Overview

Intelligent food categorization system that guides users to properly categorize foods with IBS impact attributes, building a personalized food database over time.

## 🎯 Objectives

### Primary Goals
1. **Reorganize food categories** from IBS-trigger-based to actual food type categories
2. **Add hidden IBS attributes** to foods for analysis (not visible in quick-add)
3. **Guide users** to categorize new foods they add (educational approach)
4. **Pre-populate database** with common IBS-trigger foods already categorized
5. **Auto-updating quick-add** shortcuts sorted by usage frequency

### User Experience Goals
- Users don't need to be IBS/FODMAP experts
- Simple, guided categorization for new foods
- Edit existing food attributes via long-press
- High IBS-impact foods already in database (most new foods = low impact)
- **Smart sorting:** Foods sorted by usage count (most used first), then alphabetically for equal usage

## 🏗️ Architecture

### New Category System
**12 categories** (fits 3-column layout: 4 rows × 3 columns)

1. **GRAINS** - Bread, pasta, rice, cereals, oats
2. **PROTEINS** - Meat, fish, eggs, tofu, tempeh
3. **DAIRY** - Milk, cheese, yogurt, cream
4. **FRUITS** - All fruits
5. **VEGETABLES** - All vegetables (including potatoes)
6. **LEGUMES** - Beans, lentils, chickpeas, peas
7. **NUTS_SEEDS** - Nuts, seeds, nut butters
8. **BEVERAGES** - Coffee, tea, juice, soda, water, alcohol
9. **SWEETS** - Desserts, candy, chocolate, ice cream
10. **FATS_OILS** - Butter, oil, avocado, mayo, dressings
11. **PREPARED_FOODS** - Ready meals, fast food, processed foods
12. **OTHER** - Miscellaneous items

### IBS Attributes (11 Total)
Organized into logical groups:

**FODMAP Level** (Required - Radio Buttons):
- `HIGH_FODMAP` / `MODERATE_FODMAP` / `LOW_FODMAP` (default)

**Composition** (Checkboxes):
- `GLUTEN_CONTAINING` - Contains wheat, barley, rye
- `LACTOSE_CONTAINING` - Contains dairy lactose

**Content Type** (Checkboxes):
- `HIGH_FAT` - Fried, fatty, oily foods
- `HIGH_FIBER` - High fiber content
- `SPICY` - Spicy/hot foods
- `ARTIFICIAL_SWEETENER` - Contains sorbitol, xylitol, etc.

**Beverage Specific** (Checkboxes - only show for BEVERAGES category):
- `CAFFEINATED` - Contains caffeine
- `CARBONATED` - Fizzy/carbonated drinks
- `ALCOHOLIC` - Contains alcohol
- `ACIDIC` - Acidic drinks (citrus, vinegar)

## 🔄 User Flows

### Flow 1: Adding New Food
```
User types "potatoes oven" in search
  ↓
Food not found in database
  ↓
"Add New Food" dialog appears
  ├─ Enter food name: "Potatoes - Oven Cooked"
  ├─ Select category: VEGETABLES
  ├─ FODMAP Level: ○ High ○ Moderate ● Low (default)
  ├─ Attributes: ☐ Gluten ☐ Lactose ☐ High Fat ☐ High Fiber
  └─ Save → Added to database
  ↓
Food now appears in VEGETABLES quick-add (if used frequently)
```

### Flow 2: Editing Existing Food
```
Navigate to category: BEVERAGES
  ↓
Long press on "Coffee"
  ↓
"Edit Food Attributes" dialog
  ├─ Category: BEVERAGES
  ├─ FODMAP: ● Low
  ├─ Attributes: ☑ Caffeinated ☐ Carbonated ☐ Acidic
  └─ Save changes
  ↓
Updated in database, continue with food entry
```

### Flow 3: Quick Add (Existing Food)
```
Open Food screen
  ↓
See categorized quick-add shortcuts
  (sorted by usage count DESC, then alphabetically)
  ↓
Tap "Coffee" → Confirmation dialog → Add
```

## 📊 Database Changes

### New Schema
- `FoodCategory` enum - 12 categories (replaces old 9)
- `IBSImpact` enum - 11 impact attributes
- `FoodItem` table - add `ibsImpacts` column (List<IBSImpact>)
- `CommonFood` table (new) - Pre-populated foods with categorization

### Migration Strategy
1. Create new enums and tables
2. Migrate existing `FoodItem` entries from old categories to new
3. Assign default IBS attributes based on old category
4. Pre-populate `CommonFood` table with ~100 common IBS-trigger foods

## 🎨 UI/UX Design

### Material Design 3 Compliance
- Category cards with color + icon
- 3-column grid layout (responsive)
- Bottom sheets for categorization dialogs
- Chips for IBS attributes
- Tooltips for education

### Color Palette
Following Material Design 3 color system with semantic meanings:
- Warm colors for potential triggers
- Cool colors for generally safe foods
- Balanced, accessible contrast ratios

See: `docs/food-categories/UI_UX_DESIGN.md`

## 📝 Documentation Structure

This feature has comprehensive documentation across multiple files:

```
docs/
├── FEATURE_SMART_FOOD_CATEGORIZATION.md (this file) ← Overview
└── food-categories/
    ├── DATABASE_SCHEMA.md       - Database design & migration
    ├── UI_UX_DESIGN.md          - UI specifications & mockups
    ├── IBS_ATTRIBUTES.md        - IBS attribute definitions
    ├── COMMON_FOODS.md          - Pre-populated food list (~150 foods)
    └── PROGRESS.md              - Development progress tracker
```

### 📄 Document Descriptions

**[DATABASE_SCHEMA.md](food-categories/DATABASE_SCHEMA.md)**
- New enum definitions (FoodCategory, IBSImpact, AttributeCategory)
- Updated FoodItem entity schema
- New CommonFood and FoodUsageStats tables
- Complete migration strategy (v8 → v9)
- Type converters for new types
- DAO interfaces and repository methods

**[UI_UX_DESIGN.md](food-categories/UI_UX_DESIGN.md)**
- Material Design 3 color palette and typography
- Complete screen layouts (grid, detail, dialogs)
- 3-column responsive category grid design
- Add/Edit food dialog specifications
- Long-press menu interactions
- Accessibility guidelines (WCAG AA compliance)
- Animation and transition specifications

**[IBS_ATTRIBUTES.md](food-categories/IBS_ATTRIBUTES.md)**
- Detailed explanation of all 11 IBS attributes
- Medical rationale for each attribute
- Common food examples per attribute
- User-facing tooltips and descriptions
- Attribute organization strategy
- Smart hiding/showing logic
- User education approach

**[COMMON_FOODS.md](food-categories/COMMON_FOODS.md)**
- Pre-populated list of ~150 common foods
- Organized by all 12 categories
- FODMAP classifications verified
- English + French localization
- Search terms for each food
- Data format for import
- SQL generation templates

**[PROGRESS.md](food-categories/PROGRESS.md)**
- Development phase breakdown (6 phases)
- Task checklists with status tracking
- Timeline estimates (~3 weeks total)
- Current focus and next actions
- Risk assessment and mitigation
- Decision log with rationale

### 📖 How to Use This Documentation

1. **Start here** (FEATURE_SMART_FOOD_CATEGORIZATION.md) for high-level overview
2. **Read DATABASE_SCHEMA.md** before implementing data layer
3. **Read UI_UX_DESIGN.md** before implementing UI
4. **Reference IBS_ATTRIBUTES.md** for attribute logic and tooltips
5. **Use COMMON_FOODS.md** for pre-population data
6. **Track PROGRESS.md** throughout development

## 🚀 Development Phases

### Phase 1: Foundation (Current)
- [x] Create feature branch
- [ ] Write documentation
- [ ] Design database schema
- [ ] Design UI/UX specifications

### Phase 2: Data Layer
- [ ] Create new enum classes
- [ ] Update FoodItem model
- [ ] Create CommonFood model
- [ ] Implement database migration
- [ ] Pre-populate common foods

### Phase 3: UI Layer
- [ ] Redesign category selection UI
- [ ] Create "Add New Food" dialog
- [ ] Create "Edit Food" dialog
- [ ] Update FoodScreen with new categories
- [ ] Implement long-press edit

### Phase 4: Business Logic
- [ ] Auto-updating quick-add (usage-based sorting)
- [ ] IBS attribute assignment logic
- [ ] Food search with fuzzy matching
- [ ] Category color/icon system

### Phase 5: Testing & Polish
- [ ] Test migration with real data
- [ ] User testing with guided categorization
- [ ] Performance optimization
- [ ] Documentation for users

### Phase 6: Release
- [ ] Merge to main
- [ ] Create v1.9.0 release
- [ ] Update changelog

## ⚠️ Risks & Considerations

### Technical Risks
- **Database migration complexity** - Migrating existing foods correctly
- **Performance** - Large food database, frequent searches
- **Data loss** - Ensure migration preserves all historical data

### UX Risks
- **User overwhelm** - Too many attributes to assign
  - *Mitigation:* Smart defaults, skip/assign later option
- **Category confusion** - Users unsure which category to pick
  - *Mitigation:* Clear category names, tooltips, examples

### Mitigation Strategies
1. **Thorough testing** with backup/restore before migration
2. **Phased rollout** - Test with small user group first
3. **Rollback plan** - Ability to revert to v1.8.x if needed
4. **User education** - In-app tutorials, tooltips, examples

## 📚 References

- **FODMAP Diet:** Monash University FODMAP Database
- **Material Design 3:** https://m3.material.io/
- **IBS Triggers:** Mayo Clinic IBS Guidelines
- **Android Room Migration:** https://developer.android.com/training/data-storage/room/migrating-db-versions

## 📞 Questions & Decisions Log

### Q1: FODMAP Default Value
**Decision:** Default to LOW_FODMAP
**Rationale:** Most high/moderate FODMAP foods pre-populated in database

### Q2: Maximum Categories
**Decision:** 12 categories (fits 3-column layout)
**Rationale:** Not too many to overwhelm, covers all food types

### Q3: Attribute UI Type
**Decision:** Mix of radio buttons (FODMAP) and checkboxes (attributes)
**Rationale:** FODMAP is mutually exclusive, attributes can overlap

### Q4: Edit vs. Add Flow
**Decision:** Same dialog, pre-filled for edit
**Rationale:** Code reuse, consistent UX

---

**Last Updated:** 2025-10-21
**Branch Status:** Active Development
**Next Milestone:** Complete Phase 1 documentation
