# Feature Specification: Smart Food Categorization System

**Feature Branch**: `001-smart-food-categorization`
**Created**: 2025-10-21
**Status**: Draft
**Input**: User description: "food categorisation: in the current branch, help me specify the food categorisation and quick add implementation, using docs/FEATURE_SMART_FOOD_CATEGORIZATION.md and docs/food-categories/*.md that explain in great details what we want to do."

## Clarifications

### Session 2025-10-21

- Q: What should happen if the database migration from v8 to v9 fails midway (e.g., due to insufficient storage, app crash, or data corruption)? → A: Automatic rollback to v8 + show error dialog prompting manual retry, with option to export/import data manually if retry continues to fail
- Q: How quickly should the quick-add shortcuts visually update after a user logs a food (e.g., after adding "Coffee" which increments its usage count from 11→12)? → A: Fast (<200ms) - update after database write confirms, smooth animation
- Q: For the manual export/import option (triggered after migration failures), what file format should be used for the exported food data? → A: JSON (structured, human-readable, easy to parse on re-import)
- Q: Should the food tracking data (including IBS impact attributes and symptom correlations) be encrypted at rest on the device? → A: Use EncryptedSharedPreferences for sensitive fields (balanced, Android-native)
- Q: What level of logging should be implemented for debugging migration failures, search performance issues, and data integrity problems? → A: Minimal (logcat errors only, no persistence or analytics)

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Add New Food with Guided Categorization (Priority: P1)

A user eats "potatoes - oven cooked" which doesn't exist in the quick-add database. The system guides them to categorize this new food item, teaching them about IBS impacts without overwhelming them.

**Why this priority**: Core functionality - users must be able to add foods they eat even if not in database. This is the primary value proposition and the MVP feature that delivers immediate value.

**Independent Test**: Can be fully tested by searching for a non-existent food (e.g., "homemade soup"), completing the guided categorization dialog, saving the food, and verifying it appears in the appropriate category list sorted by usage.

**Acceptance Scenarios**:

1. **Given** user is on Food screen, **When** they search for "potatoes oven", **Then** system shows "not found" and presents "Add New Food" dialog
2. **Given** "Add New Food" dialog is open, **When** user enters name "Potatoes - Oven Cooked" and selects VEGETABLES category, **Then** system shows appropriate attribute checkboxes (hides beverage-specific)
3. **Given** user hasn't assigned any attributes, **When** they save the food, **Then** system defaults to LOW_FODMAP and saves successfully
4. **Given** user selects HIGH_FIBER attribute, **When** they tap info icon (ℹ️), **Then** system shows educational tooltip: "High fiber content. Can help with constipation but may cause gas. Soluble fiber usually better tolerated."
5. **Given** user has saved custom food, **When** they return to Food screen, **Then** the new food appears in VEGETABLES category sorted by usage count (0 initially, alphabetically with others at 0)
6. **Given** user selects the newly added food, **When** they add it to their log, **Then** usage count increments and food moves up in sort order

---

### User Story 2 - Edit Existing Food Attributes (Priority: P2)

A user realizes that their "Coffee" entry should be marked as acidic in addition to caffeinated. They need to edit existing food attributes via long-press interaction.

**Why this priority**: Critical for data accuracy and user learning. As users learn more about IBS, they'll want to refine their food database. Enables continuous improvement of personal database.

**Independent Test**: Can be fully tested by navigating to BEVERAGES category, long-pressing "Coffee", selecting "Edit Attributes", adding ACIDIC checkbox, saving, and verifying the attribute appears in the food details.

**Acceptance Scenarios**:

1. **Given** user is viewing BEVERAGES category detail screen, **When** they long-press on "Coffee" item, **Then** context menu appears with "Edit Attributes" and "Delete" options
2. **Given** context menu is open, **When** user taps "Edit Attributes", **Then** Edit Food dialog opens pre-filled with current values (BEVERAGES category, LOW_FODMAP, CAFFEINATED checkbox checked)
3. **Given** Edit Food dialog is open for BEVERAGES item, **When** user checks additional attribute ACIDIC, **Then** beverage-specific section remains visible and checkbox is selected
4. **Given** user has modified attributes, **When** they tap "Save", **Then** changes persist to database and updated attributes display in food list
5. **Given** user edits a pre-populated food, **When** they save changes, **Then** system maintains link to commonFoodId but marks customizations

---

### User Story 3 - Quick Add from Usage-Sorted Shortcuts (Priority: P1)

A user frequently drinks coffee and wants to quickly log it without searching or categorizing. The quick-add shortcuts automatically surface their most-used foods.

**Why this priority**: Core UX efficiency feature. Most users eat similar foods repeatedly - quick-add based on usage patterns dramatically improves daily logging speed. Essential for MVP user experience.

**Independent Test**: Can be fully tested by adding "Coffee" 5 times, "Bread" 3 times, "Apple" 2 times, then verifying quick-add section shows them in that order (Coffee, Bread, Apple) due to usage count DESC sorting.

**Acceptance Scenarios**:

1. **Given** user opens Food screen, **When** screen loads, **Then** quick-add section displays top 6 foods sorted by usage count DESC, then alphabetically ASC for equal usage
2. **Given** user has never added any foods (usage count = 0 for all), **When** quick-add loads, **Then** top 6 foods are sorted alphabetically among those with 0 usage
3. **Given** quick-add shortcuts are displayed, **When** user taps "Coffee ☕ x12" shortcut, **Then** confirmation dialog appears with quantity and time fields pre-populated
4. **Given** confirmation dialog is open, **When** user taps "Add", **Then** food logs to database, usage count increments to 13, and quick-add re-sorts if needed
5. **Given** user's top foods change over time, **When** they return to Food screen, **Then** quick-add automatically updates to reflect current usage patterns (dynamic, not static)

---

### User Story 4 - Browse Foods by Category Grid (Priority: P2)

A user wants to explore all vegetables they've logged or could log. They navigate using the 12-category grid layout to browse organized food lists.

**Why this priority**: Secondary navigation pattern - helps discovery and exploration. Not required for basic food logging but improves findability and organization as database grows.

**Independent Test**: Can be fully tested by tapping VEGETABLES category card, viewing all vegetables sorted by usage DESC then alphabetically, searching within category, and navigating back to grid.

**Acceptance Scenarios**:

1. **Given** user is on Food screen, **When** they view category grid, **Then** 12 categories display in 3-column × 4-row layout with colors and icons per Material Design 3
2. **Given** category grid is visible, **When** user taps VEGETABLES card (green, grass icon), **Then** Category Detail Screen opens showing all vegetables
3. **Given** Category Detail Screen is open, **When** user views food list, **Then** foods are sorted by usage count DESC (most used first), then alphabetically ASC for equal usage
4. **Given** user is in Category Detail Screen, **When** they use search field to filter by "potato", **Then** only matching vegetables appear (e.g., "Potato", "Potatoes - Oven Cooked")
5. **Given** Category Detail Screen shows results, **When** user taps "+ Add New Vegetable", **Then** Add Food dialog opens with VEGETABLES category pre-selected

---

### User Story 5 - Progressive IBS Education Through Tooltips (Priority: P3)

A user new to IBS tracking doesn't understand FODMAP terms. The system educates them progressively through tooltips and simple explanations without overwhelming.

**Why this priority**: Educational feature that improves long-term engagement and accuracy. Not blocking for basic functionality but critical for user confidence and correct categorization over time.

**Independent Test**: Can be fully tested by triggering first-time tutorial overlay, tapping info icons (ℹ️) for FODMAP and attributes, and verifying simple, medical-backed explanations appear.

**Acceptance Scenarios**:

1. **Given** user adds their first custom food, **When** Add Food dialog opens, **Then** brief tutorial overlay appears: "Let's categorize this food! Don't worry - you can always edit later."
2. **Given** user views FODMAP Level section, **When** they tap info icon (ℹ️), **Then** tooltip explains: "FODMAP = Fermentable Oligo, Di, Mono-saccharides and Polyols. High FODMAP foods can trigger bloating. Most new foods you add will be LOW FODMAP (default)."
3. **Given** user checks CAFFEINATED attribute, **When** they tap info icon, **Then** tooltip shows: "Contains caffeine which stimulates bowel movement. May trigger diarrhea in IBS-D."
4. **Given** user has added 5 foods, **When** they add 6th food, **Then** system shows tip: "Did you know you can edit any food's attributes by long-pressing?"
5. **Given** user has added 10 foods, **When** dashboard loads, **Then** system shows stats: "You've added 10 foods! 70% are low FODMAP - great job!"

---

### Edge Cases

- **What happens when user searches for existing food with slight typo?** System uses case-insensitive substring matching on `name` and `searchTerms` columns to find partial matches. If "coffe" is searched, it matches "Coffee" (substring match) before offering "Add New".

- **How does system handle duplicate food names?** Prevents exact duplicates by checking `name` field case-insensitively. If "Coffee" exists, attempting to add "coffee" prompts user to edit existing or add with qualifier (e.g., "Coffee - Decaf").

- **What if user changes category of existing food?** System updates category immediately. If food has category-specific attributes (e.g., CAFFEINATED for BEVERAGES), changing to GRAINS removes beverage-specific attributes and shows warning dialog.

- **How are foods sorted when multiple have usage count = 0?** Secondary sort is alphabetical ASC. For example, three foods with 0 uses: "Apple", "Banana", "Carrot" display in that order.

- **What happens during database migration if user has 1000+ historical food entries?** Migration runs in background thread with progress indicator. Old categories map to new categories, default IBS impacts assigned. User can bulk-edit foods post-migration if needed.

- **What if database migration from v8 to v9 fails midway?** System automatically rolls back to v8 (preserving original data), displays error dialog explaining failure with "Retry Migration" button. If migration fails after 3 retry attempts, system offers "Export Data" option allowing manual backup in JSON format (structured, human-readable) and fresh v9 start.

- **How does system handle deleted common foods that have usage history?** Soft delete - mark `isVerified = false` but preserve usage stats. Food remains in user's history but doesn't appear in search for new entries.

- **What if user assigns contradictory attributes?** System warns but allows (e.g., GLUTEN + FRUITS is unusual but possible for processed fruit products). Validation is suggestive, not restrictive.

- **How is sensitive health data protected on the device?** System uses Android's EncryptedSharedPreferences for storing food tracking data, IBS impact attributes, and usage patterns. Data is encrypted using hardware-backed keys (Android Keystore) providing at-rest protection without requiring user-managed passwords.

- **How are errors logged for debugging purposes?** System logs errors and warnings to Android logcat only (no persistent file logging or analytics). Migration failures, data corruption, and search performance issues are logged with ERROR level including exception stack traces for development debugging.

## Requirements *(mandatory)*

### Functional Requirements

**Data Model & Schema:**

- **FR-001**: System MUST define 12 food categories (GRAINS, PROTEINS, DAIRY, FRUITS, VEGETABLES, LEGUMES, NUTS_SEEDS, BEVERAGES, SWEETS, FATS_OILS, PROCESSED, OTHER) with Material Design 3 colors and icons
- **FR-002**: System MUST define 11 IBS impact attributes organized in 8 AttributeCategory groups: FODMAP (3 mutually exclusive: FODMAP_HIGH, FODMAP_MODERATE, FODMAP_LOW), GRAIN_BASED (1: GLUTEN), DAIRY_BASED (1: LACTOSE), STIMULANTS (2: CAFFEINE, ALCOHOL), IRRITANTS (1: SPICY), MACRONUTRIENTS (1: FATTY), CHEMICAL (1: ACIDIC), ADDITIVES (1: ARTIFICIAL_SWEETENERS)
- **FR-003**: System MUST store FoodItem with category, ibsImpacts list, isCustom boolean, and optional commonFoodId reference
- **FR-004**: System MUST maintain CommonFood table with name, category, ibsImpacts, searchTerms, usageCount, localization (FR/EN), and verification status
- **FR-005**: System MUST track FoodUsageStats with foodName, category, totalUses, lastUsed timestamp, and commonFoodId reference

**Database Migration:**

- **FR-006**: System MUST migrate database from v8 to v9 preserving all historical user data; on migration failure, system MUST automatically rollback to v8, display error dialog with retry option (max 3 attempts), and offer manual export/import option after repeated failures
- **FR-049**: System MUST export user data in JSON format when manual export triggered, including all FoodItems with historical data, categories, IBS impacts, and usage statistics; JSON structure MUST be human-readable and re-importable
- **FR-007**: System MUST map old 9 categories to new 12 categories using defined mapping (DAIRY→DAIRY, GLUTEN→GRAINS, HIGH_FODMAP→OTHER, SPICY→PROCESSED, CAFFEINE→BEVERAGES, FATTY→FATS_OILS, ALCOHOL→BEVERAGES, RAW→OTHER, PROCESSED→PROCESSED)
- **FR-008**: System MUST assign IBS impacts based on old category (DAIRY→LACTOSE+FODMAP_HIGH, GLUTEN→GLUTEN+FODMAP_HIGH, HIGH_FODMAP→FODMAP_HIGH, SPICY→SPICY+FODMAP_LOW, CAFFEINE→CAFFEINE+FODMAP_LOW, FATTY→FATTY+FODMAP_LOW, PROCESSED→FODMAP_MODERATE, ALCOHOL→ALCOHOL+FODMAP_LOW, default→FODMAP_LOW)
- **FR-009**: System MUST pre-populate CommonFood table with approximately 150 verified foods covering all categories with bilingual names (EN/FR)
- **FR-010**: System MUST build initial usage statistics from existing food_items table during migration

**Food Addition & Categorization:**

- **FR-011**: System MUST present "Add New Food" dialog when user searches for non-existent food
- **FR-012**: Users MUST be able to enter food name, select category from 12 options via horizontal scroll selector
- **FR-013**: System MUST require FODMAP level selection with LOW_FODMAP as default value
- **FR-014**: System MUST display attribute checkboxes organized in sections: Composition, Content Type, Beverage Specific (conditional)
- **FR-015**: System MUST show Beverage Specific attributes (CAFFEINATED, CARBONATED, ALCOHOLIC, ACIDIC) ONLY when category = BEVERAGES
- **FR-016**: System MUST allow users to skip attribute selection (saves with LOW_FODMAP default only)
- **FR-017**: System MUST save custom foods with isCustom = true and isVerified = false

**Food Editing:**

- **FR-018**: Users MUST be able to long-press any food item to access context menu with "Edit Attributes" and "Delete" options (Note: "Add Favorite" is a future feature not included in v1.9.0)
- **FR-019**: System MUST open Edit Food dialog pre-filled with current category and attributes when "Edit Attributes" selected
- **FR-020**: System MUST allow changing category, FODMAP level, and all applicable attributes
- **FR-021**: System MUST warn user if changing category removes category-specific attributes (e.g., BEVERAGES → GRAINS removes CAFFEINATED)
- **FR-022**: System MUST allow deletion of custom foods (isCustom = true) but prevent deletion of verified common foods

**Quick Add & Sorting:**

- **FR-023**: System MUST display quick-add section showing top 6 most-used foods on Food screen, dynamically updated based on current usage patterns (not static), with automatic re-sorting when usage counts change
- **FR-024**: System MUST sort all food lists by usage count DESC (highest first), then alphabetically ASC for equal usage
- **FR-025**: System MUST increment usageCount in both CommonFood and FoodUsageStats tables when food is added to log
- **FR-027**: System MUST display usage count badge on quick-add shortcuts (e.g., "Coffee ☕ x12")
- **FR-048**: System MUST update quick-add shortcuts within 200ms after database write confirms, using smooth animation to reflect new usage counts and re-sorted order

**Category Navigation:**

- **FR-028**: System MUST display 12-category grid in 3-column layout on Food screen (responsive: 2 columns on small screens, 4 on tablets)
- **FR-029**: Users MUST be able to tap category card to open Category Detail Screen showing all foods in that category
- **FR-030**: System MUST sort foods in Category Detail Screen by usage count DESC, then alphabetically ASC
- **FR-031**: Category Detail Screen MUST include search field for filtering foods within category
- **FR-032**: Category Detail Screen MUST display "+ Add New [Category]" button with category pre-selected

**Search & Autocomplete:**

- **FR-033**: System MUST search CommonFood table by name and searchTerms fields using case-insensitive substring matching (SQL LIKE '%query%' operator on both name and searchTerms columns)
- **FR-034**: System MUST display search results sorted by usage count DESC with limit of 20 results
- **FR-035**: System MUST show "Add new [query]" option when search returns no results
- **FR-036**: System MUST support bilingual search (EN and FR) matching against both name and nameFr/nameEn fields

**User Education:**

- **FR-037**: System MUST display info icons (ℹ️) next to FODMAP Level and attribute sections
- **FR-038**: System MUST show educational tooltips when info icons tapped, containing medical rationale and examples
- **FR-039**: System MUST display first-time tutorial overlay when user adds first custom food
- **FR-040**: System MUST show progressive tips at milestones (after 5 foods: "long-press to edit", after 10 foods: stats summary)
- **FR-041**: System MUST provide "?" help link to comprehensive IBS Attributes guide (glossary)

**UI/UX Compliance:**

- **FR-042**: System MUST follow Material Design 3 guidelines for all UI components (colors, typography, spacing)
- **FR-043**: System MUST ensure minimum touch targets of 48dp × 48dp for all interactive elements
- **FR-044**: System MUST maintain WCAG AA accessibility compliance with 4.5:1 contrast ratio for body text
- **FR-045**: System MUST use bottom sheet presentation for Add/Edit Food dialogs
- **FR-046**: System MUST support screen readers with proper contentDescription for all icons and images
- **FR-047**: System MUST implement ripple effects, state changes, and smooth animations per Material Design

**Security & Privacy:**

- **FR-050**: System MUST use Android EncryptedSharedPreferences (backed by Android Keystore) to encrypt sensitive food tracking data, IBS impact attributes, and usage statistics at rest
- **FR-051**: System MUST handle EncryptedSharedPreferences decryption failures gracefully by falling back to regular SharedPreferences and notifying user of encryption reset (preserving data integrity over encryption enforcement)

**Observability:**

- **FR-052**: System MUST log migration failures, data corruption errors, and critical exceptions to Android logcat with ERROR level including full stack traces and contextual data (database version, operation attempted, error message)
- **FR-053**: System MUST NOT persist logs to device storage or transmit to external analytics services (minimal logging approach)

### Key Entities *(include if feature involves data)*

- **FoodCategory (Enum)**: Represents 12 actual food types (GRAINS, PROTEINS, DAIRY, FRUITS, VEGETABLES, LEGUMES, NUTS_SEEDS, BEVERAGES, SWEETS, FATS_OILS, PROCESSED, OTHER). Each has Material Design 3 color (Color) and icon (ImageVector). Used for organizing foods by what they ARE, not by IBS impact.

- **IBSImpact (Enum)**: Represents 11 IBS impact attributes with displayName, description, and AttributeCategory grouping. Three mutually exclusive FODMAP levels (FODMAP_HIGH, FODMAP_MODERATE, FODMAP_LOW), grain-based trigger (GLUTEN), dairy-based trigger (LACTOSE), stimulants (CAFFEINE, ALCOHOL), irritant (SPICY), macronutrient (FATTY), chemical property (ACIDIC), additive (ARTIFICIAL_SWEETENERS). Grouped into 8 AttributeCategory enums for UI organization. Used for hidden analysis attributes.

- **AttributeCategory (Enum)**: Groups IBSImpact attributes for UI organization: FODMAP (radio buttons), COMPOSITION (checkboxes), CONTENT (checkboxes), BEVERAGE (conditional checkboxes).

- **FoodItem (Entity)**: User's logged food entries. Core fields: id, name, quantity, date, notes. New fields: category (FoodCategory), ibsImpacts (List<IBSImpact>), isCustom (Boolean), commonFoodId (Long?). Represents actual consumption events with categorization.

- **CommonFood (Entity)**: Pre-populated and user-added food database. Fields: id, name, category, ibsImpacts, searchTerms (List<String>), usageCount, nameFr, nameEn, isVerified, createdAt. Represents reusable food definitions, sorted by usage for quick-add.

- **FoodUsageStats (Entity)**: Tracks usage patterns for smart sorting. Fields: foodName (primary key), category, totalUses, lastUsed, commonFoodId. Enables usage-based quick-add and category sorting.

- **DataRepository**: Centralized data access layer exposing CommonFoodDao and FoodUsageStatsDao methods. Handles food search, usage tracking, custom food creation, and category filtering. Returns Flow<List<T>> for reactive UI updates.

- **FoodViewModel**: Domain layer managing food-related state. Holds StateFlow for common foods by category, search results, quick-add shortcuts, and dialog states. Calls repository methods and updates UI state reactively.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can categorize new custom foods in under 2 minutes including attribute selection (measured via user testing with 10 participants, average time < 120 seconds)

- **SC-002**: Quick-add shortcuts surface the correct 6 most-used foods 100% of the time after usage data accumulates (verified via automated tests comparing usage_count sorting)

- **SC-003**: Database migration from v8 to v9 completes successfully with 0% data loss for 100% of test cases (verified with migration tests on databases with 10, 100, 1000, 5000 food entries)

- **SC-004**: Food search returns relevant results in under 1 second for 95% of queries (measured via performance profiling with database of 500+ foods, p95 latency < 1000ms)

- **SC-005**: 90% of users successfully complete first custom food addition on first attempt without errors (measured via analytics tracking completion rate of Add Food dialog)

- **SC-006**: Category grid layout renders correctly on 100% of device sizes from 360dp to 800dp width (verified via manual testing on small phones, standard phones, phablets, and tablets)

- **SC-007**: All UI components meet WCAG AA accessibility standards with 100% of text meeting 4.5:1 contrast ratio (verified via automated accessibility scanner and manual TalkBack testing)

- **SC-008**: Sorting behavior is consistent across all screens: usage count DESC, then alphabetically ASC, with 100% accuracy (verified via unit tests for DAO queries and UI integration tests)

- **SC-009**: Pre-populated common foods database contains at least 150 verified foods covering all 12 categories with bilingual names (verified via database inspection post-migration)

- **SC-010**: Educational tooltips are triggered successfully 100% of the time when info icons tapped (verified via instrumented UI tests on all dialogs)

- **SC-011**: Long-press gesture detection works correctly on 100% of supported Android devices (tested on API 24+ with different screen densities and touch sensitivities)

- **SC-012**: App memory usage stays under 200MB during typical usage session with 500+ foods in database (verified via Android Studio Profiler, no memory leaks detected)

- **SC-013**: Quick-add shortcuts update within 200ms after food logging completes in 95% of cases (measured via performance profiling, p95 latency < 200ms from database write confirmation to UI re-render)

- **SC-014**: JSON export/import roundtrip preserves 100% of user data including all FoodItems, categories, IBS impacts, usage counts, and timestamps with zero data loss (verified via automated tests comparing pre-export and post-import database states)

- **SC-015**: Sensitive food tracking data is encrypted at rest using EncryptedSharedPreferences on 100% of supported devices API 24+ (verified via code inspection and device testing, encryption confirmed via Android Keystore integration)
