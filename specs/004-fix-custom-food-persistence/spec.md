# Feature Specification: Fix Custom Food Addition Bug

**Feature Branch**: `004-fix-custom-food-persistence`
**Created**: 2025-10-27
**Status**: Draft
**Input**: User description: "fix custom food addition bug. Have a look at the TODO item **Food not appearing after adding to a category** to understand the bug."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Add Custom Food to Category (Priority: P1)

A user wants to track a food that isn't in the pre-populated list. They select a category (e.g., "Other"), enter a custom food name (e.g., "Soja"), and expect to see it in the category list and be able to select it again later.

**Why this priority**: This is the core bug - without this working, users cannot build their personalized food database, which is a fundamental feature of the app.

**Independent Test**: Can be fully tested by adding a custom food to any category and verifying it appears in both the category detail screen and search results.

**Acceptance Scenarios**:

1. **Given** a user is on the Food screen, **When** they select a category (e.g., "Other") and enter a custom food name "Soja" and save, **Then** the food "Soja" appears in the "Other" category list immediately
2. **Given** a user has added "Soja" to the "Other" category, **When** they navigate to the "Other" category detail screen, **Then** "Soja" appears in the food list alongside pre-populated foods
3. **Given** a user has added "Soja", **When** they use the search bar to search for "Soja", **Then** "Soja" appears in the search results
4. **Given** a user has used "Soja" multiple times, **When** they view the "Other" category, **Then** "Soja" is sorted by usage count (most used first) then alphabetically

---

### User Story 2 - Category Display with Custom Foods (Priority: P2)

Users want to see a balanced mix of their most-used custom foods and pre-populated foods when viewing a category, with usage-based ordering.

**Why this priority**: This ensures a good UX where users see their personalized foods first while still having access to suggested foods.

**Independent Test**: Can be tested by adding several custom foods to a category and verifying the top 6 foods displayed follow the usage-based sorting rule.

**Acceptance Scenarios**:

1. **Given** a category has both custom and pre-populated foods, **When** the user views the category, **Then** the display shows up to 6 foods sorted by usage count (descending) then alphabetically (ascending)
2. **Given** a category has fewer than 6 used foods, **When** the user views the category, **Then** unused pre-populated foods fill the remaining slots to reach 6 total foods
3. **Given** a user has logged "Soja" 10 times and a pre-populated food "Rice" 5 times, **When** they view the category, **Then** "Soja" appears before "Rice" in the list

---

### User Story 3 - Quick-Add for Custom Foods (Priority: P3)

Users want their frequently used custom foods to appear in the quick-add row (top 4 most used foods across all categories) for fast logging.

**Why this priority**: This optimizes the workflow for power users who have established their personalized food list.

**Independent Test**: Can be tested by logging a custom food multiple times and verifying it appears in the quick-add shortcuts.

**Acceptance Scenarios**:

1. **Given** a user has used custom food "Soja" more than any pre-populated food, **When** they open the Food screen, **Then** "Soja" appears in the quick-add row (top 4 foods)
2. **Given** the quick-add row shows 4 foods, **When** a user adds and frequently uses a new custom food, **Then** the quick-add row updates to reflect the new usage statistics

---

### Edge Cases

- What happens when a user enters a custom food name that matches an existing pre-populated food name?
  - System should recognize the match and use the existing CommonFood entry instead of creating a duplicate

- What happens when the usage count for custom foods becomes very large (e.g., > 1000)?
  - System should continue sorting correctly without performance degradation

- What happens when a user adds a custom food with special characters or very long names?
  - System should validate input and handle edge cases gracefully (max length enforcement, special character handling)

- What happens when a user adds many custom foods to a category (e.g., > 50)?
  - Category view should still show top 6 by usage, search should return all matches up to limit (50)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST create a `CommonFood` entry when a user adds a custom food to a category
- **FR-002**: Custom `CommonFood` entries MUST have `is_verified = 0` to distinguish them from pre-populated foods
- **FR-003**: System MUST check if a custom food name matches an existing `CommonFood` before creating a new entry
- **FR-004**: Category detail screens MUST display foods from the `common_foods` table (both verified and user-added)
- **FR-005**: Category lists MUST show top 6 foods sorted by usage count (descending) then alphabetically (ascending)
- **FR-006**: If a category has fewer than 6 used foods, MUST fill remaining slots with unused pre-populated foods
- **FR-007**: Search functionality MUST include both pre-populated and user-added custom foods
- **FR-008**: Quick-add row MUST show top 4 most-used foods across all categories (including custom foods)
- **FR-009**: When saving a `FoodItem`, system MUST link it to the corresponding `CommonFood` entry via `commonFoodId`
- **FR-010**: System MUST increment `CommonFood.usage_count` when a food is logged
- **FR-011**: System MUST populate default IBS attributes for custom foods (FODMAP_LOW as default)
- **FR-012**: System MUST set the category for custom `CommonFood` entries to match the selected category

### Key Entities

- **CommonFood**: Represents both pre-populated and user-added foods with attributes including:
  - `id`: Unique identifier
  - `name`: Food name (e.g., "Soja")
  - `category`: FoodCategory enum value
  - `ibsImpacts`: List of IBS attributes (FODMAP levels, triggers, properties)
  - `usage_count`: Number of times food has been logged
  - `is_verified`: Boolean flag (true for pre-populated, false for user-added)
  - `search_terms`: Alternative names for fuzzy matching

- **FoodItem**: Represents a logged food entry with:
  - `id`: Unique identifier
  - `name`: Food name at time of logging
  - `category`: Category at time of logging
  - `commonFoodId`: Foreign key to `CommonFood` (nullable for backward compatibility)
  - `timestamp`: When the food was logged
  - `isCustom`: Boolean flag indicating if originally custom (deprecated in favor of checking `CommonFood.is_verified`)

- **FoodUsageStats**: Tracks usage statistics for foods:
  - `foodName`: Name of the food
  - `category`: Category of the food
  - `usageCount`: Number of times logged
  - `lastUsed`: Timestamp of last usage

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can add a custom food and see it appear in the category list within 1 second
- **SC-002**: Custom foods persist across app restarts and appear in search results
- **SC-003**: Category lists display correct mix of top 6 foods (by usage, then alphabetically)
- **SC-004**: Quick-add row updates dynamically to show top 4 most-used foods (including custom foods)
- **SC-005**: No duplicate `CommonFood` entries are created when users re-add existing food names
- **SC-006**: System handles at least 200 custom foods per category without performance degradation (p95 < 500ms for category load)
- **SC-007**: Search returns custom foods within 1 second for queries up to 50 results
- **SC-008**: 100% of custom food additions result in visible entries in both category views and search results
