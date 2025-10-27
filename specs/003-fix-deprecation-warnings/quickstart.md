# Quick Start: Fixing Deprecation Warnings

**Feature**: Fix Deprecation Compilation Warnings
**Branch**: `003-fix-deprecation-warnings`
**For**: Developers implementing the deprecation fixes

## Overview

This guide provides step-by-step instructions for fixing all 13 deprecation warnings in the IBS Tracker Android app. Each fix is straightforward and maintains 100% behavioral compatibility.

---

## Prerequisites

- ✅ Checked out branch: `003-fix-deprecation-warnings`
- ✅ Android Studio Arctic Fox or later
- ✅ Kotlin 1.8.20+
- ✅ Read [research.md](./research.md) for detailed rationale

---

## Quick Reference: All Fixes

| File | Line | Warning | Fix | Priority |
|------|------|---------|-----|----------|
| PatternsTab.kt | 77 | Icons.Filled.TrendingUp | → Icons.AutoMirrored.Filled.TrendingUp | P1 |
| PatternsTab.kt | 420 | Icons.Filled.TrendingUp | → Icons.AutoMirrored.Filled.TrendingUp | P1 |
| AnalysisScreen.kt | 97 | Icons.Filled.Help | → Icons.AutoMirrored.Filled.Help | P1 |
| AnalysisScreen.kt | 958 | Icons.Filled.HelpOutline | → Icons.AutoMirrored.Filled.HelpOutline | P1 |
| AnalysisScreen.kt | 1341 | Icons.Filled.ArrowBack | → Icons.AutoMirrored.Filled.ArrowBack | P1 |
| AnalysisScreen.kt | 1360 | Icons.Filled.ArrowForward | → Icons.AutoMirrored.Filled.ArrowForward | P1 |
| AnalysisScreen.kt | 230 | Indicator | → SecondaryIndicator | P1 |
| CommonFoods.kt | 38 | getCommonFoods() | Remove (use CommonFoodDao) | P1 |
| AnalysisScreen.kt | 1323 | LinearProgressIndicator(Float) | → LinearProgressIndicator { } | P2 |
| CredentialManagerAuth.kt | 132 | Condition always true | Simplify logic | P2 |
| ViewModelFactory.kt | 35 | Unchecked cast | Add @Suppress | P2 |
| Theme.kt | 96 | statusBarColor | → Window Insets | P3 |
| LocaleHelper.kt | 30 | updateConfiguration() | → createConfigurationContext() | P3 |

---

## Step-by-Step Fixes

### Phase 1: Material Icons (P1 - Quick Wins)

#### Fix 1-6: AutoMirrored Icons (6 files, ~5 minutes)

**Files**: `PatternsTab.kt`, `AnalysisScreen.kt`

**Steps**:

1. **Add AutoMirrored import** (if not present):
   ```kotlin
   import androidx.compose.material.icons.automirrored.filled.TrendingUp
   import androidx.compose.material.icons.automirrored.filled.Help
   import androidx.compose.material.icons.automirrored.filled.HelpOutline
   import androidx.compose.material.icons.automirrored.filled.ArrowBack
   import androidx.compose.material.icons.automirrored.filled.ArrowForward
   ```

2. **Replace icon references**:
   ```kotlin
   // Before
   Icon(Icons.Filled.TrendingUp, ...)

   // After
   Icon(Icons.AutoMirrored.Filled.TrendingUp, ...)
   ```

3. **Remove old imports** (if no longer used):
   ```kotlin
   // Remove these if no other usages exist
   import androidx.compose.material.icons.filled.TrendingUp
   import androidx.compose.material.icons.filled.Help
   // ... etc
   ```

4. **Verify**:
   ```bash
   ./gradlew compileDebugKotlin | grep "TrendingUp\|Help\|Arrow"
   # Should show zero deprecation warnings for these icons
   ```

**Test**: Open Analytics screen → verify icons display correctly (no visual changes expected)

---

### Phase 1: UI Components (P1 - Quick Wins)

#### Fix 7: Indicator → SecondaryIndicator (~2 minutes)

**File**: `AnalysisScreen.kt:230`

**Steps**:

1. **Find and replace**:
   ```kotlin
   // Before
   Indicator(
       modifier = ...,
       height = ...,
       color = ...
   )

   // After
   SecondaryIndicator(
       modifier = ...,
       height = ...,
       color = ...
   )
   ```

2. **Verify import**:
   ```kotlin
   import androidx.compose.material3.SecondaryIndicator
   ```

**Test**: Navigate tabs in Analytics screen → verify tab indicator displays correctly

---

#### Fix 8: Remove Deprecated Function (~1 minute)

**File**: `CommonFoods.kt:38`

**Steps**:

1. **Verify no usages exist**:
   ```bash
   grep -r "getCommonFoods(" app/src/main/
   # Should return zero results (function already replaced with CommonFoodDao)
   ```

2. **Remove the deprecated function**:
   ```kotlin
   // Delete this entire function
   @Deprecated("Use CommonFoodDao.getCommonFoodsByCategory() instead")
   fun getCommonFoods(context: Context, category: FoodCategory): List<String> {
       // ... function body ...
   }
   ```

**Test**: Build succeeds, no references to removed function

---

### Phase 2: Moderate Effort (2-4 hours)

#### Fix 9: LinearProgressIndicator Lambda (~15 minutes)

**File**: `AnalysisScreen.kt:1323`

**Steps**:

1. **Identify current progress value**:
   ```kotlin
   // Current code (example)
   val analysisProgress by viewModel.progress.collectAsState()
   LinearProgressIndicator(
       progress = analysisProgress, // Float value
       modifier = ...,
       color = ...,
       trackColor = ...,
       strokeCap = ...
   )
   ```

2. **Wrap progress in lambda**:
   ```kotlin
   LinearProgressIndicator(
       progress = { analysisProgress }, // Lambda returning Float
       modifier = ...,
       color = ...,
       trackColor = ...,
       strokeCap = ...
   )
   ```

3. **Alternative: If progress is complex expression**:
   ```kotlin
   LinearProgressIndicator(
       progress = {
           // Can include complex calculations
           (completedTasks.toFloat() / totalTasks).coerceIn(0f, 1f)
       },
       modifier = ...,
       color = ...,
       trackColor = ...,
       strokeCap = ...
   )
   ```

**Test**: Trigger analysis → verify progress bar animates smoothly

---

#### Fix 10: Simplify Always-True Condition (~10 minutes)

**File**: `CredentialManagerAuth.kt:132`

**Steps**:

1. **Read the context around line 132** to understand the logic

2. **Common patterns to fix**:

   **Pattern A**: Redundant boolean check
   ```kotlin
   // Before
   if (someBoolean == true) { ... }

   // After
   if (someBoolean) { ... }
   ```

   **Pattern B**: Literal true condition
   ```kotlin
   // Before
   if (true) {
       doSomething()
   }

   // After
   doSomething() // Just execute directly
   ```

   **Pattern C**: Always-true comparison
   ```kotlin
   // Before
   val result = if (x > 0 || true) { ... } else { ... }

   // After
   val result = ... // Just use the true branch
   ```

3. **Verify with unit tests**:
   ```bash
   ./gradlew test --tests "*CredentialManagerAuth*"
   ```

**Test**: Run authentication tests → verify login/credential flow works correctly

---

#### Fix 11: Add Unchecked Cast Suppression (~5 minutes)

**File**: `ViewModelFactory.kt:35`

**Steps**:

1. **Verify safety** (check exists):
   ```kotlin
   if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
       @Suppress("UNCHECKED_CAST") // Safe: type checked above with isAssignableFrom
       val filterPreferencesManager = FilterPreferencesManager(context)
       return AnalyticsViewModel(analysisRepository, dataRepository, filterPreferencesManager) as T
   }
   ```

2. **Add suppression with documentation**:
   ```kotlin
   @Suppress("UNCHECKED_CAST") // Safe: type checked with isAssignableFrom() above
   return SomeViewModel(...) as T
   ```

**Rationale**: Standard Android ViewModel factory pattern - cast is safe due to type check

**Test**: Open any screen with ViewModel → verify ViewModels inject correctly

---

### Phase 3: Requires Careful Testing (4-6 hours)

#### Fix 12: statusBarColor → Window Insets (~2 hours)

**File**: `Theme.kt:96`

**Steps**:

1. **Understand current usage**:
   ```kotlin
   // Current (deprecated)
   window.statusBarColor = Color.TRANSPARENT.toArgb()
   ```

2. **Replace with modern edge-to-edge approach**:
   ```kotlin
   // Add import
   import androidx.core.view.WindowCompat

   // In IBSTrackerTheme or MainActivity.onCreate
   @Composable
   fun IBSTrackerTheme(
       darkTheme: Boolean = isSystemInDarkTheme(),
       content: @Composable () -> Unit
   ) {
       val colorScheme = when {
           darkTheme -> darkColorScheme()
           else -> lightColorScheme()
       }

       val view = LocalView.current
       if (!view.isInEditMode) {
           SideEffect {
               val window = (view.context as Activity).window
               WindowCompat.setDecorFitsSystemWindows(window, false)
               window.statusBarColor = Color.TRANSPARENT.toArgb()

               // Set light/dark status bar icons
               val insetsController = WindowCompat.getInsetsController(window, view)
               insetsController?.isAppearanceLightStatusBars = !darkTheme
           }
       }

       MaterialTheme(
           colorScheme = colorScheme,
           typography = Typography,
           content = content
       )
   }
   ```

3. **Test across Android versions**:
   - Android 7.0 (API 26): Basic status bar
   - Android 10 (API 29): Dark mode switching
   - Android 11+ (API 30+): Edge-to-edge with gestures

**Test**:
- Light theme: Status bar icons should be dark
- Dark theme: Status bar icons should be light
- Swipe from edges: No UI clipping
- Toggle dark/light mode: Status bar updates correctly

---

#### Fix 13: updateConfiguration() → createConfigurationContext() (~2 hours)

**File**: `LocaleHelper.kt:30`

**Steps**:

1. **Current code analysis**:
   ```kotlin
   // Current (deprecated)
   fun setLocale(context: Context, languageCode: String): Context {
       val locale = Locale(languageCode)
       Locale.setDefault(locale)
       val config = Configuration(context.resources.configuration)
       config.setLocale(locale)
       context.resources.updateConfiguration(config, context.resources.displayMetrics)
       return context
   }
   ```

2. **Migrate to createConfigurationContext()**:
   ```kotlin
   // Modern approach
   fun setLocale(context: Context, languageCode: String): Context {
       val locale = Locale(languageCode)
       Locale.setDefault(locale)

       val config = Configuration(context.resources.configuration)
       config.setLocale(locale)

       // Create new context instead of updating existing
       return context.createConfigurationContext(config)
   }
   ```

3. **Update caller in MainActivity**:
   ```kotlin
   override fun attachBaseContext(newBase: Context) {
       val container = (newBase.applicationContext as IBSTrackerApplication).container
       val languageCode = runBlocking {
           container.settingsRepository.languageFlow.first().code
       }

       // Use returned context (not mutating newBase)
       val localeContext = LocaleHelper.setLocale(newBase, languageCode)
       super.attachBaseContext(localeContext)
   }
   ```

**Test**:
- Change language in Settings (English ↔ French)
- Verify all UI text updates
- Restart app → verify language persists
- Check date/number formatting

---

## Verification Checklist

After all fixes are complete:

### Build Verification

```bash
# 1. Clean build
./gradlew clean

# 2. Compile with all warnings
./gradlew compileDebugKotlin --warning-mode all

# Expected: Zero deprecation warnings

# 3. Compile release build
./gradlew compileReleaseKotlin --warning-mode all

# Expected: Zero deprecation warnings

# 4. Full build (includes lint)
./gradlew build

# Expected: BUILD SUCCESSFUL
```

### Test Suite

```bash
# 1. Unit tests
./gradlew test

# Expected: All tests pass (100% pass rate)

# 2. Instrumented tests
./gradlew connectedAndroidTest

# Expected: All tests pass

# 3. Test report location
open app/build/reports/tests/testDebugUnitTest/index.html
```

### Manual Testing

- [ ] Open app → verify no crashes
- [ ] Navigate to Dashboard → verify UI renders
- [ ] Navigate to Analytics → verify icons display
- [ ] Check progress indicators → verify animation
- [ ] Toggle dark/light theme → verify status bar
- [ ] Change language setting → verify locale updates
- [ ] Add food entry → verify data saves
- [ ] Add symptom → verify data saves
- [ ] Run analysis → verify results display

---

## Common Issues & Solutions

### Issue 1: Import Conflicts

**Problem**: Multiple `TrendingUp` imports available

**Solution**:
```kotlin
// Remove
import androidx.compose.material.icons.filled.TrendingUp

// Keep
import androidx.compose.material.icons.automirrored.filled.TrendingUp
```

### Issue 2: Progress Indicator Not Animating

**Problem**: Lambda-based progress doesn't update

**Solution**: Ensure progress value is wrapped in lambda:
```kotlin
// Wrong
progress = progressValue

// Correct
progress = { progressValue }
```

### Issue 3: Status Bar Color Not Changing

**Problem**: Status bar stays same color in dark mode

**Solution**: Verify `isAppearanceLightStatusBars` updates:
```kotlin
insetsController?.isAppearanceLightStatusBars = !darkTheme
```

### Issue 4: Locale Not Persisting

**Problem**: Language resets after app restart

**Solution**: Verify `attachBaseContext()` uses returned context:
```kotlin
val localeContext = LocaleHelper.setLocale(newBase, languageCode)
super.attachBaseContext(localeContext) // Use localeContext, not newBase
```

---

## Success Criteria

✅ **Build**: Zero deprecation warnings in both debug and release builds
✅ **Tests**: 100% test pass rate maintained
✅ **UI**: All screens render correctly with no visual regressions
✅ **Functionality**: All features work identically to before fixes
✅ **Performance**: No measurable performance degradation

---

## Commit Strategy

**Recommended approach**: One commit per category for easy review/revert

```bash
# Commit 1: Material Icons (Fixes 1-6)
git add app/src/main/java/com/tiarkaerell/ibstracker/ui/components/analysis/PatternsTab.kt
git add app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/AnalysisScreen.kt
git commit -m "fix: Migrate Material Icons to AutoMirrored variants

- Replace Icons.Filled.* with Icons.AutoMirrored.Filled.*
- Fixes 6 deprecation warnings for RTL-compatible icons
- Zero behavioral change (icons identical in LTR languages)"

# Commit 2: Compose Components (Fixes 7-8)
git add app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/AnalysisScreen.kt
git add app/src/main/java/com/tiarkaerell/ibstracker/data/model/CommonFoods.kt
git commit -m "fix: Update deprecated Compose components

- Replace Indicator with SecondaryIndicator
- Remove deprecated getCommonFoods() function
- Fixes 2 deprecation warnings"

# Commit 3: Progress Indicator (Fix 9)
git add app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/AnalysisScreen.kt
git commit -m "fix: Migrate LinearProgressIndicator to lambda-based progress

- Wrap progress value in lambda for future animation support
- Maintains current behavior while using modern API"

# Commit 4: Code Quality (Fixes 10-11)
git add app/src/main/java/com/tiarkaerell/ibstracker/data/auth/CredentialManagerAuth.kt
git add app/src/main/java/com/tiarkaerell/ibstracker/ui/viewmodel/ViewModelFactory.kt
git commit -m "fix: Address code quality warnings

- Simplify always-true condition in CredentialManagerAuth
- Add @Suppress for unavoidable unchecked cast in ViewModelFactory
- Both changes maintain existing functionality"

# Commit 5: Android Framework APIs (Fixes 12-13)
git add app/src/main/java/com/tiarkaerell/ibstracker/ui/theme/Theme.kt
git add app/src/main/java/com/tiarkaerell/ibstracker/utils/LocaleHelper.kt
git commit -m "fix: Migrate deprecated Android framework APIs

- Replace statusBarColor with Window Insets for edge-to-edge
- Replace updateConfiguration() with createConfigurationContext()
- Tested across Android 7.0 - 14 (API 26-34)"
```

---

## Next Steps

After completing all fixes:

1. ✅ Push branch and create PR
2. ✅ Request code review
3. ✅ Address review feedback
4. ✅ Merge to main
5. ✅ Monitor for any regressions in production

---

## References

- [research.md](./research.md) - Detailed rationale for each fix
- [spec.md](./spec.md) - Feature specification
- [plan.md](./plan.md) - Implementation plan
- [Android Developers: Migrate deprecated APIs](https://developer.android.com/jetpack/androidx/releases)
- [Jetpack Compose Release Notes](https://developer.android.com/jetpack/androidx/releases/compose)
