# Research: Fix Custom Food Addition Bug

**Feature**: 004-fix-custom-food-persistence
**Date**: 2025-10-27
**Status**: Complete

## Research Questions

### Q1: How should we handle duplicate food name detection?

**Decision**: Case-sensitive exact match using `CommonFoodDao.getCommonFoodByName()`

**Rationale**:
- Room already provides `getCommonFoodByName(name: String): Flow<CommonFood?>` query
- Case-sensitive matches user expectation ("Soja" vs "soja" could be different foods)
- Exact match prevents false positives (e.g., "Rice" shouldn't match "Rice flour")
- Existing pre-populated foods use proper capitalization consistently

**Alternatives Considered**:
1. **Case-insensitive fuzzy matching** - Rejected: Over-complicated for MVP, could create false duplicates
2. **Levenshtein distance** - Rejected: Performance overhead, unnecessary complexity
3. **User confirmation dialog** - Rejected: Adds friction to common workflow

**Implementation**:
```kotlin
// In DataRepository.insertFoodItem()
val existingCommonFood = getCommonFoodByName(foodItem.name).first()
```

---

### Q2: What default IBS attributes should custom foods have?

**Decision**: Default to `listOf(IBSImpact.FODMAP_LOW)` for custom foods

**Rationale**:
- Safest assumption: Unknown foods treated as low-FODMAP by default
- Matches medical guidance: "unknown → assume safe until proven otherwise"
- User can manually update IBS attributes later via long-press edit (future feature)
- Consistent with migration pattern in `Migration_8_9.kt` for existing foods

**Alternatives Considered**:
1. **Empty list** - Rejected: Violates CommonFood contract requiring exactly one FODMAP level
2. **FODMAP_MODERATE** - Rejected: Too conservative, discourages food logging
3. **Prompt user for IBS attributes** - Rejected: Adds friction, can be added later as enhancement

**Implementation**:
```kotlin
val defaultIbsImpacts = listOf(IBSImpact.FODMAP_LOW)
```

---

### Q3: Should we support batch duplicate checking for performance?

**Decision**: No batch optimization needed for MVP

**Rationale**:
- Users add one custom food at a time via UI (not bulk imports)
- Single `getCommonFoodByName()` query is fast (<10ms for 500 foods)
- Room uses indexed queries on `name` column
- Premature optimization: No evidence of performance bottleneck

**Alternatives Considered**:
1. **Cache CommonFood names in memory** - Rejected: Adds state management complexity
2. **Batch insert API** - Rejected: No current use case for bulk custom food creation
3. **Debounce duplicate checks** - Rejected: Single query already fast enough

**Performance Validation**:
- Room indexed query on `name` column: O(log n)
- Expected dataset: < 1000 CommonFood entries
- p95 latency: < 10ms per lookup

---

### Q4: How to handle backward compatibility with existing FoodItems?

**Decision**: Make `commonFoodId` optional (nullable) and backfill retroactively

**Rationale**:
- Schema v9 already has `common_food_id` as nullable column
- Existing FoodItems without `commonFoodId` continue working
- Future enhancement: Migration to backfill `commonFoodId` for old entries
- No breaking changes to existing data

**Alternatives Considered**:
1. **Required migration** - Rejected: Adds complexity, risks data loss
2. **Ignore old entries** - Rejected: Creates inconsistent behavior
3. **Dual-path logic** - Rejected: Increases code complexity

**Backward Compatibility Strategy**:
- New custom foods: Always create CommonFood + link via `commonFoodId`
- Old FoodItems: Continue using `name` + `category` for display
- Future: Add migration task to backfill `commonFoodId` (out of scope)

---

### Q5: Should search terms be auto-generated for custom foods?

**Decision**: Leave `search_terms` empty for custom foods in MVP

**Rationale**:
- Pre-populated foods have manually curated search terms (e.g., "yogurt" → "yoghurt, yogourt")
- No reliable algorithm for auto-generating meaningful search terms
- Custom food `name` is already searched via `LIKE` query
- Future enhancement: Allow user to add search terms via edit dialog

**Alternatives Considered**:
1. **Copy name to search_terms** - Rejected: Redundant, no value added
2. **NLP-based term generation** - Rejected: Over-engineered for Android app
3. **Crowdsourced suggestions** - Rejected: Requires backend, out of scope

**Implementation**:
```kotlin
val newCommonFood = CommonFood(
    name = foodItem.name,
    category = foodItem.category,
    ibsImpacts = defaultIbsImpacts,
    isVerified = false,
    searchTerms = "" // Empty for custom foods
)
```

---

## Best Practices Applied

### Room Database Patterns

**Pattern**: Repository-mediated database access
- All DB operations go through `DataRepository`
- DAOs are private to data layer
- UI layer never accesses DAOs directly
- Reactive updates via `Flow<T>` for UI consistency

**Pattern**: Suspend functions for write operations
- All inserts/updates are suspend functions
- Executed on background thread via `viewModelScope.launch`
- No risk of main thread blocking

**Pattern**: Flow for read operations
- `Flow<List<T>>` provides reactive updates
- UI automatically updates when data changes
- No manual refresh required

### Kotlin Coroutines Best Practices

**Pattern**: Use `.first()` to get single value from Flow
```kotlin
// Get immediate value from Flow without collecting
val existing = getCommonFoodByName(name).first()
```

**Pattern**: Transaction atomicity
- Room operations are atomic by default
- Multiple insert operations wrapped in single transaction if needed
- Use `@Transaction` annotation for complex multi-table operations

### Android Testing Best Practices

**Pattern**: Test pyramid
- Unit tests: Repository logic (duplicate detection, default values)
- Integration tests: Room DAO queries with in-memory database
- Instrumented tests: End-to-end custom food creation flow

**Pattern**: Test data builders
- Create reusable test fixtures for CommonFood, FoodItem
- Separate verified vs custom food test data
- Parameterized tests for edge cases

---

## Technology Decisions

### No New Dependencies Required

All functionality can be implemented using existing dependencies:
- Room 2.6.1: Already has all needed DAO methods
- Kotlin Coroutines: Already using Flow and suspend functions
- Jetpack Compose: Already displaying CommonFood data

### No Database Migration Required

Schema v9 already supports all needed fields:
- `common_foods.is_verified` column exists
- `food_items.common_food_id` column exists (nullable)
- `food_items.category` column exists
- No schema changes needed

### Performance Optimization

**Indexing**: Room automatically indexes primary keys
- `common_foods.name` would benefit from index for lookup performance
- Can be added in future migration if needed (current dataset < 500 foods)

**Query Optimization**:
- Use `LIMIT` clauses where applicable (search results, top N queries)
- Avoid N+1 queries by using Flow for reactive updates
- Single transaction for insert + usage count update

---

## Risk Assessment

### Low Risk Items ✅

- **Data integrity**: Room enforces foreign key constraints
- **UI reactivity**: Flow automatically triggers recomposition
- **Backward compatibility**: Nullable `commonFoodId` handles old data
- **Performance**: Single indexed lookup per insert (< 10ms)

### Medium Risk Items ⚠️

- **Duplicate detection edge cases**:
  - Risk: User adds "Soja" then "soja" (different cases)
  - Mitigation: Case-sensitive exact match is documented behavior
  - Future: Add case-insensitive suggestion in UI

- **Default FODMAP_LOW assumption**:
  - Risk: User adds high-FODMAP food, gets incorrect analysis
  - Mitigation: Analytics show uncertainty for low-data foods
  - Future: Prompt user for IBS attributes during creation

### Mitigated Risks 🛡️

- **Schema changes**: None required (v9 already supports feature)
- **Breaking changes**: None (all changes additive)
- **Data migration**: None required (nullable fields handle old data)

---

## Open Questions for Future Enhancements

1. **Edit custom food IBS attributes**: Long-press to edit `ibsImpacts` (P2)
2. **Backfill `commonFoodId` for old FoodItems**: Migration to link historical data (P3)
3. **Case-insensitive duplicate suggestions**: "Did you mean 'Soja'?" UI prompt (P3)
4. **Auto-generated search terms**: NLP or user-provided alternatives (P4)
5. **Bulk import custom foods**: CSV import for power users (P4)

---

## References

- Existing codebase: `DataRepository.kt` (lines 51-70)
- Room documentation: [Flow support](https://developer.android.com/training/data-storage/room/async-queries)
- Migration pattern: `Migration_8_9.kt` (CommonFood creation examples)
- DAO contracts: `CommonFoodDao.kt`, `FoodItemDao.kt`
