# Research Findings: Deprecation Warning Migrations

**Branch**: `002-fix-deprecation-warnings` | **Date**: 2025-10-24
**Purpose**: Document migration patterns for all deprecated API replacements

---

## Research Task 0.1: FoodCategory Display Name Migration

### Decision

Replace with **direct property access using `category.displayName`** to maintain current behavior (English-only).

### Rationale

1. The deprecated `FoodCategoryHelper.getDisplayName(context, category)` **always returns English** (`category.displayName`)
2. The `context` parameter is **never used** by the implementation - it was accepted but completely ignored
3. For exact behavioral parity, replacing with `category.displayName` maintains identical behavior
4. The deprecated function provided **no internationalization** despite accepting a Context parameter

### Migration Pattern

```kotlin
// Before (deprecated)
FoodCategoryHelper.getDisplayName(context, category)

// After (current - exact behavioral parity)
category.displayName
```

### Optional Enhancement: Add Internationalization Support

If internationalization is desired for food categories:

```kotlin
// Option with locale awareness (enhanced functionality beyond deprecated API)
val isFrench = Locale.getDefault().language == "fr"
FoodCategory.getDisplayName(category, isFrench)

// OR use Settings Repository (most robust)
val language by settingsViewModel.language.collectAsState()
FoodCategory.getDisplayName(category, isFrench = language == Language.FRENCH)
```

### Behavioral Notes

- **No locale detection**: The deprecated function did NOT perform any locale-based switching
- **Unused context**: The `context` parameter was never used in the implementation
- **Non-breaking change**: Using `category.displayName` maintains current English-only behavior
- **Internationalization gap**: Despite FR-012 stating "i18n must continue to work", the deprecated API never provided i18n

### Files Affected

- `AnalyticsScreen.kt` - 2 occurrences (lines 268, 339)
- `DashboardScreen.kt` - 6 occurrences (lines 189, 204, 216, 221, 542, 555)
- `FoodScreen.kt` - 7 occurrences (lines 243, 426, 501, 507, 532, 676, 724)

**Total: 15 occurrences across 3 files**

---

## Research Task 0.2: Compose Material3 Component Migration

### Component 1: Divider → HorizontalDivider

**Decision**: Direct 1:1 replacement with HorizontalDivider

**Rationale**:
- Compose Material3 1.2.0-alpha04 renamed `Divider` to `HorizontalDivider` for clarity
- All parameters remain identical (modifier, thickness, color)
- No visual or behavioral differences
- Simple rename with no functional changes

**Migration Pattern**:

```kotlin
// Before
Divider(modifier = ..., thickness = ..., color = ...)

// After
HorizontalDivider(modifier = ..., thickness = ..., color = ...)
```

**Behavioral Notes**:
- Maintains identical appearance and spacing
- Added `VerticalDivider` as separate component for vertical divisions
- Material3 theme compatibility unchanged

**Files Affected**:
- `DashboardScreen.kt` - 2 occurrences (lines 498, 508)
- `SettingsScreen.kt` - 3 occurrences (lines 702, 747, 775)

**Total: 5 occurrences across 2 files**

---

### Component 2: menuAnchor() Signature

**Decision**: Use `MenuAnchorType.PrimaryNotEditable` for all occurrences (all are read-only selection dropdowns)

**Rationale**:
- Material3 1.3.0-alpha04 added `enabled` parameter and required `type` parameter for better accessibility semantics
- All 4 occurrences in codebase are read-only selection dropdowns (not editable text input)
- `PrimaryNotEditable` provides correct semantics for selection-only dropdowns
- `enabled` parameter defaults to `true`, maintaining current behavior

**Migration Pattern**:

```kotlin
// Before (deprecated)
Modifier.menuAnchor()

// After
Modifier.menuAnchor(
    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
    enabled = true  // default value, can be omitted
)
```

**MenuAnchorType Values**:
- `ExposedDropdownMenuAnchorType.PrimaryNotEditable` - For read-only text fields (our use case)
- `ExposedDropdownMenuAnchorType.PrimaryEditable` - For editable text fields
- `ExposedDropdownMenuAnchorType.SecondaryEditable` - For trailing icons in editable text fields

**Behavioral Notes**:
- `enabled` parameter defaults to `true`, can be omitted for default behavior
- When `enabled = false`, menu won't expand/collapse and semantics are hidden from accessibility services
- Dropdown menu positioning and interaction behavior unchanged with appropriate defaults

**Files Affected**:
- `DashboardScreen.kt` - 1 occurrence (line 204) - category selection dropdown
- `SettingsScreen.kt` - 3 occurrences:
  - Line 135 - language selection (readOnly = true)
  - Line 184 - units selection (readOnly = true)
  - Line 724 - sex selection (TextButton, not TextField)

**Total: 4 occurrences across 2 files**

---

## Research Task 0.3: KeyboardOptions Constructor Migration

### Decision

Use explicit `autoCorrectEnabled = false` for password fields to maintain current behavior.

### Rationale

1. **Semantic preservation**: `autoCorrect = false` explicitly disabled autocorrect, so `autoCorrectEnabled = false` preserves this intent
2. **Security best practice**: Password fields should never have autocorrect enabled
3. **Explicit over implicit**: Being explicit about disabling autocorrect for sensitive input is clearer and more maintainable
4. **User experience**: Prevents autocorrect suggestions from appearing on password entry

### Migration Pattern

```kotlin
// Before (deprecated)
KeyboardOptions(
    autoCorrect = false,
    keyboardType = KeyboardType.Password
)

// After (current API)
KeyboardOptions(
    autoCorrectEnabled = false,
    keyboardType = KeyboardType.Password
)
```

### Behavioral Notes

**Three-state behavior of `autoCorrectEnabled`:**

- **`false`**: Explicitly disables auto-correction (use for password fields, email fields, etc.)
- **`true`**: Explicitly enables auto-correction (use for text content, messages, notes, etc.)
- **`null`** (default): No preference specified, defers to platform defaults (autocorrect enabled by default)

**Important considerations**:
- The autocorrect option is **largely ignored** by many keyboard implementations (serves as a hint only)
- There's **no guarantee** all keyboards will respect this setting
- The nullable design enables proper **merging of KeyboardOptions** when InputTransformations are combined
- Setting to `null` is equivalent to not specifying the parameter at all

### Files Affected

- `SettingsScreen.kt` - 4 occurrences (all password input fields):
  - Line 1301 - Google email password
  - Line 1327 - Google email password (confirmation)
  - Line 1417 - Another password field
  - Line 1500 - Another password field

**Total: 4 occurrences in 1 file**

---

## Research Task 0.4: Google Drive API Transport Migration

### Decision

Replace `AndroidHttp.newCompatibleTransport()` with `NetHttpTransport()`

### Rationale

1. **Official deprecation**: AndroidHttp is deprecated with message: "Gingerbread is no longer supported by Google Play Services"
2. **Historical context**: AndroidHttp was created to handle pre-Gingerbread SDK compatibility issues with HttpURLConnection
3. **Modern compatibility**: Project's minSdk is 26 (Android 8.0), far above the Gingerbread threshold
4. **Library availability**: `google-http-client:1.44.1` (which contains NetHttpTransport) is already available as transitive dependency
5. **No behavioral difference**: For SDK 26+, AndroidHttp.newCompatibleTransport() internally returns NetHttpTransport anyway

### Migration Pattern

```kotlin
// Before (deprecated)
import com.google.api.client.extensions.android.http.AndroidHttp

Drive.Builder(
    AndroidHttp.newCompatibleTransport(),
    GsonFactory(),
    requestInitializer
)

// After (current)
import com.google.api.client.http.javanet.NetHttpTransport

Drive.Builder(
    NetHttpTransport(),
    GsonFactory(),
    requestInitializer
)
```

### Behavioral Notes

**No functional changes**:
- NetHttpTransport uses standard Java `HttpURLConnection` implementation
- Thread-safe HTTP transport based on `java.net` package
- For SDK 26+, functionally identical to what AndroidHttp.newCompatibleTransport() returns
- No authentication or configuration differences required

**Performance considerations**:
- Documentation recommends single globally-shared instance for maximum efficiency
- Current implementation creates new instance per getDriveService() call (acceptable but not optimal)

### Files Affected

- `GoogleDriveBackup.kt` - 2 occurrences:
  - Line 4 - import statement
  - Line 297 - usage in getDriveService() method

**Total: 2 occurrences (1 import + 1 usage) in 1 file**

---

## Research Task 0.5: CommonFoods DAO Migration

### Decision

Replace deprecated `CommonFoods.getCommonFoods()` with Room DAO access via `DataRepository.getCommonFoodsByCategory()`

### Rationale

1. **Database-first architecture**: Deprecated function returns `emptyList()` - it's a legacy stub. New system stores foods in Room database with ~150 pre-populated entries
2. **Reactive data streams**: DAO returns `Flow<List<CommonFood>>` enabling reactive UI updates vs static `List<String>`
3. **Rich data model**: Returns `CommonFood` entities with complete metadata (category, IBS impacts, usage counts, translations) vs just name strings
4. **Performance & caching**: Room provides query optimization, threading, and caching
5. **Single source of truth**: All food data in database, not scattered across helper objects

### Migration Pattern

```kotlin
// ❌ Before (deprecated - returns empty list)
import com.tiarkaerell.ibstracker.data.model.CommonFoods
val foods: List<String> = CommonFoods.getCommonFoods(context, category)
// Returns: emptyList()

// ✅ After (current approach via repository)
// Access through DataRepository (already exposed in FoodViewModel)
val commonFoods: Flow<List<CommonFood>> =
    repository.getCommonFoodsByCategory(category)

// In Composable UI code:
val commonFoodsState by viewModel.getCommonFoodsByCategory(category)
    .collectAsState(initial = emptyList())

// Extract food names if needed:
val foodNames: List<String> = commonFoodsState.map { it.name }
```

### Behavioral Notes

**Return type change**:
- **Old**: `List<String>` (synchronous, food names only)
- **New**: `Flow<List<CommonFood>>` (reactive, full entities)

**Threading requirements**:
- **Old**: Ran on caller's thread (but returned empty list)
- **New**: Flow collection on Dispatchers.IO automatically (Room handles threading)
- **UI collection**: Use `.collectAsState()` in Composables

**Data availability**:
- **Old**: Always returned `emptyList()` immediately
- **New**: Returns Flow emitting ~10-25 foods per category from pre-populated database
- **Sorting**: Results sorted by `usage_count DESC, name ASC` (most-used first)

**Actual usage location**:
The deprecated function is only called internally within `CommonFoods.kt` itself (line 34) by the `searchFoods()` method. Since `getCommonFoods()` returns `emptyList()`, the entire search mechanism is obsolete and should be replaced with `CommonFoodDao.searchCommonFoods()`.

### Files Affected

Primary changes:
- `CommonFoods.kt` - Remove or deprecate `getCommonFoods()` and `searchFoods()` methods
- `FoodScreen.kt` - Replace `CommonFoods.FoodSearchResult` usage with `CommonFood` entities (line 55)
- `FoodViewModel.kt` - Add `searchCommonFoods(query: String): Flow<List<CommonFood>>` method

**Total: 1 occurrence (internal call) + associated search functionality migration**

---

## Summary of All Migrations

| Priority | Category | Occurrences | Complexity | Risk Level |
|----------|----------|-------------|------------|------------|
| P1 | FoodCategory.getDisplayName | 15 | Low | Low |
| P2 | Divider → HorizontalDivider | 5 | Very Low | Very Low |
| P2 | menuAnchor() signature | 4 | Low | Low |
| P3 | KeyboardOptions constructor | 4 | Very Low | Very Low |
| P4 | AndroidHttp transport | 2 | Low | Medium |
| P5 | CommonFoods helper | 1 + search | Medium | Medium |

**Total**: 31+ occurrences across 8 source files

### Risk Assessment

**Low Risk (P1-P3)**: Direct API replacements with identical behavior
- Simple find/replace operations
- Well-documented migration paths
- Immediate visual/functional verification possible

**Medium Risk (P4-P5)**: Require testing with external dependencies
- P4: Google Drive sync needs real account testing
- P5: Search functionality changes require UI validation

### Testing Strategy

1. **After P1**: Verify all screens display food categories correctly in both English and French (if i18n added)
2. **After P2**: Visual inspection - UI components render identically
3. **After P3**: Password fields function correctly without autocorrect
4. **After P4**: Complete backup/restore cycle with Google Drive account
5. **After P5**: Search functionality returns expected results from database

---

**Research Complete**: 2025-10-24
**All NEEDS CLARIFICATION items resolved**: Yes ✅
**Ready for Phase 1 (quickstart.md generation)**: Yes ✅
