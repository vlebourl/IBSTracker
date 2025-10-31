## Completed ✅

### v1.13.2 - Cloud Auto-Backup Storage Management
* ~~**Cloud backups filling up storage**~~ **[Completed - Released v1.13.2]**
  - ~~Issue: Daily scheduled cloud syncs create timestamped files, accumulating indefinitely~~
  - ~~Solution: Auto-backups now use fixed filenames that overwrite previous versions~~
  - ~~Auto cloud backups: "auto_cloud_backup_v2.json" (overwrites daily)~~
  - ~~Manual cloud backups: Timestamped filenames (preserves all user-initiated backups)~~
  - ~~Matches local backup behavior for consistency~~
  - ~~Added isAutoBackup parameter throughout backup chain~~
  - ~~GoogleDriveBackup.createBackup() deletes existing auto-backup before upload~~
  - ~~Manual "Sync Now" button uses timestamped names (isAutoBackup=false)~~
  - ~~Scheduled WorkManager sync uses fixed names (isAutoBackup=true default)~~
  - ~~Comprehensive logging for auto-backup file detection and deletion~~
  - ~~Prevents cloud storage bloat while preserving important manual backups~~

### v1.13.1 - Cloud Backup Deletion
* ~~**Cloud backup deletion not working**~~ **[Completed - Released v1.13.1]**
  - ~~Issue: GoogleDriveService.deleteCloudBackup() returned false (stub implementation)~~
  - ~~Solution: Implemented Drive API deletion using files().delete(fileId)~~
  - ~~Added GoogleDriveBackup.deleteBackup() method with Drive API integration~~
  - ~~Updated BackupViewModel to retrieve and pass access token for cloud deletion~~
  - ~~Comprehensive logging throughout deletion flow for debugging~~
  - ~~BackupViewModel logs: backup type, access token retrieval, results~~
  - ~~GoogleDriveService logs: API call execution~~
  - ~~GoogleDriveBackup logs: Drive service initialization, deletion success/failure~~
  - ~~Cloud backup deletion now fully functional with proper error handling~~

### v1.12.0 - Custom Food Persistence Fix
* ~~**Food not appearing after adding to a category**~~ **[Completed - Released v1.12.0]**
  - ~~Issue: Adding "Soja" to Other category doesn't show in category after save~~
  - ~~Issue: Added food doesn't appear as existing in search bar~~
  - ~~Root cause: Custom foods saved as FoodItem without creating CommonFood entries~~
  - ~~Fix: Modified DataRepository.insertFoodItem() to create/link CommonFood entries~~
  - ~~Testing: 30 comprehensive tests (15 tests × 2 devices), 100% passing~~
  - ~~Categories sorted by usage_count DESC, name ASC~~
  - ~~Custom foods appear in quick-add (top 4 most-used)~~
  - ~~Performance validated: < 500ms with 200+ foods, search < 1s~~
  - ~~Edge cases: UTF-8 support, case-sensitive, large usage counts (1500+)~~
  - ~~No database migration required (schema v10 unchanged)~~
  - ~~Backward compatible (nullable commonFoodId)~~
  - ~~Single file modification (DataRepository.kt)~~

### v1.11.1 - Deprecation Warnings Elimination
* ~~**Fix all 13 deprecation compilation warnings**~~ **[Completed - Released v1.11.1]**
  - ~~Migrated 6 Material Icons to AutoMirrored variants (TrendingUp, Help, HelpOutline, ArrowBack, ArrowForward)~~
  - ~~Replaced TabRowDefaults.Indicator with SecondaryIndicator~~
  - ~~Removed deprecated getCommonFoods() and searchFoods() functions~~
  - ~~Wrapped LinearProgressIndicator progress in lambda for animation support~~
  - ~~Simplified always-true condition in CredentialManagerAuth~~
  - ~~Improved @Suppress placement in ViewModelFactory~~
  - ~~Migrated statusBarColor to WindowCompat with edge-to-edge display~~
  - ~~Replaced updateConfiguration() with createConfigurationContext()~~
  - ~~Zero behavioral changes - 100% functional parity maintained~~
  - ~~All tests passing - verified on emulator~~

### v1.11.0 - Phase 1 Analytics Enhancement
* ~~**Phase 1 Analytics Implementation**~~ **[Completed - Released v1.11.0]**
  - ~~Symptom-centric analysis with probability correlations~~
  - ~~Enhanced pattern detection with temporal and severity analysis~~
  - ~~Improved insights with actionable recommendations~~
  - ~~3-hour symptom window (clinical standard)~~
  - ~~30-minute meal grouping~~
  - ~~PRIMARY meal-level trigger analysis~~
  - ~~SECONDARY individual food analysis with isolation tracking~~
  - ~~Confidence indicators (VERY_LOW to HIGH based on occurrence count)~~
  - ~~Co-occurrence warnings~~
  - ~~Collapsible UI cards for all three trigger types~~
  - ~~Material Design 3 styled analytics cards~~

### v1.10.0 - Previous Analytics Work
* ~~Fix colors in food selection~~ **[Completed - Category cards properly colored]**

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
* **Error handling in ViewModels** - Users need feedback on failed operations (~3-4 hours)
  - Add sealed class `UiState<T>` with Loading/Success/Error states
  - Expose error states from FoodViewModel, SymptomsViewModel
  - Show error messages in UI (Snackbar or AlertDialog)
  - Handle database operation failures gracefully
* **Input validation** - Prevent invalid data entry (~1-2 hours)
  - Validate food/symptom names (not empty, reasonable length)
  - Validate symptom intensity (1-10 range)
  - Validate timestamps (not in future)
  - Show validation errors before save attempt
* **Enhanced sync status indicator** - Improve Google Drive backup state visibility (~1-2 hours)
  - ✅ Last successful backup timestamp already shown in settings
  - ✅ Visual feedback during backup operations implemented
  - TODO: Indicate pending/failed backups with retry option
  - TODO: Add sync status badge to dashboard
  - TODO: Show detailed sync history (last 5 syncs with success/failure status)

### ⚡ Quick Wins
**Priority**: High-value, low-effort improvements (< 1 day each)
* **Auto daily backup enhancements** - Additional backup features (~1-2 hours)
  - ✅ WorkManager for daily backup scheduling (implemented)
  - ✅ Local and cloud backup with auto-overwrite (implemented)
  - ✅ Settings toggles for local/cloud backups (implemented)
  - ✅ Restore functionality from backup files (implemented)
  - TODO: Keep configurable number of manual backups (currently keeps all)
  - TODO: Add automatic cleanup of old manual backups (e.g., keep last 30)
* **Category ordering by usage frequency** - UX improvement (~2-3 hours)
  - Sort food categories by usage count (most used first)
  - Use existing FoodUsageStats infrastructure
  - Backend sorting only (no visible counter)
* **Symptoms page UI revamp** - Modernize and align with Food page (~3-4 hours)
  - Apply same Material Design 3 styling as Food page
  - Add search functionality (filter symptoms by name)
  - Modernize list layout with cards
  - Add visual intensity indicators (colored bars/icons)
  - Improve edit/delete workflows
  - Match Food page architecture and UX patterns
  - Better date/time display formatting
* **Skip quick-add confirmation setting** - Faster logging workflow (~1 hour)
  - Add settings toggle "Skip confirmation on quick-add"
  - Show brief toast/snackbar feedback instead of dialog
  - Significantly speeds up frequent food logging
* **Loading states throughout app** - Better UX feedback (~2-3 hours)
  - Add CircularProgressIndicator when fetching data
  - Show loading during analytics calculations
  - Show progress during backup/restore operations
  - Prevent duplicate saves during loading
* **Reusable DateTimePicker component** - DRY principle (~2 hours)
  - Extract duplicated date/time picker logic
  - Create shared composable DateTimePickerDialog
  - Use consistently across Dashboard, Food, Symptoms screens
* **Database performance indexes** - Query optimization (~30 minutes)
  - Add index on Symptom.date column
  - Add index on FoodItem.timestamp column
  - Improves analytics query performance with large datasets
* **Crash reporting setup** - Production debugging (~2 hours)
  - Integrate Firebase Crashlytics or similar
  - Capture unhandled exceptions
  - Track non-fatal errors
  - Add user context (app version, device info)

### ⭐ New Features
**Priority**: Major functionality additions
* **Add medication tracking** - New tracking category with medication name, dosage, time, and integration with symptom correlation (~3-5 days)
  - Add to "Symptoms" tab or create separate "Medications" section
  - Pre-populate common IBS medications
  - Track medication-symptom correlations
* **Body weight history** - Track weight changes over time (~2-3 days)
  - Add weight entry UI
  - Display trend graph in analysis page
  - Correlate with symptom patterns
* **Bulk operations** - Select multiple entries for batch actions (~4-5 hours)
  - Multi-select mode with checkboxes
  - Bulk delete with confirmation
  - Bulk export functionality
  - Select all / deselect all actions
* **Notes field on entries** - Add context to food/symptoms (~3-4 hours + migration)
  - Add optional "notes" field to FoodItem and Symptom models
  - Database migration to add column
  - Show notes in timeline and detail views
  - Useful for recording context like "ate quickly" or "stressful day"
* **Export to PDF/CSV** - Share reports with healthcare providers (~5-6 hours)
  - Export timeline data to CSV format
  - Generate PDF report with analytics summary
  - Include charts and trigger analysis
  - Share via Android share sheet
* **Reminders & Notifications** - Improve tracking consistency (~4-5 hours)
  - Customizable meal/symptom logging reminders
  - Use WorkManager for reliable scheduling
  - Notification settings (times, frequency)
  - Snooze functionality
* **Undo functionality** - Safety net for accidental deletions (~5-6 hours)
  - Soft delete architecture (mark deleted, keep data)
  - Snackbar with "Undo" action after delete
  - Auto-purge soft-deleted items after 30 days
  - Restore functionality in settings

### 🎨 UI/UX Polish
**Priority**: Visual consistency and user experience
* **Comprehensive 2025 Material Design 3 alignment** - Full app UI/UX review and modernization (~10-14 days)
  - Review entire app UI against latest Material Design 3 (2025) guidelines
  - Align all screens, panes, and tabs for visual consistency
  - Harmonize navigation patterns (tabs, bottom nav, top bar)
  - Ensure consistent spacing, typography, and component usage
  - Modernize color palette across food, categories, quick-adds, symptoms, analysis
  - Fix color inconsistencies (dairy brightness vs fruits contrast)
  - Standardize interaction patterns (swipes, taps, long-press)
  - Update elevation, shadows, and surface treatments
  - Apply motion and animation guidelines
  - Create comprehensive design system documentation
* **Color harmonization** - Ensure consistent color scheme across all screens (~2-3 hours)
  - Fix dairy brightness vs fruits contrast
  - Harmonize food, categories, quick-adds, symptoms, analysis colors
  - Apply Material Design 3 color palette consistently
* **Dashboard timeline enhancements** - Better filtering and search (~4-5 hours)
  - Add filter by entry type (food only / symptoms only / all)
  - Add search functionality across timeline
  - Add date range selector (last 7 days / 30 days / custom)
  - Improve empty state with illustrations
* **Onboarding tutorial** - Help new users understand features (~5-6 hours)
  - First-run tutorial explaining key features
  - Tooltips for analytics concepts (meal grouping, confidence levels)
  - Optional skip functionality
  - "What's New" dialog on version updates
* **Empty state improvements** - More engaging empty screens (~2-3 hours)
  - Add illustrations or animations for empty states
  - Actionable buttons (e.g., "Log your first meal")
  - Brief explanations of features
* **Dark mode toggle in settings** - User preference override (~2 hours)
  - Add setting to force dark/light mode
  - Override system setting if desired
  - Persist preference

### 📊 Analytics & Insights (Phase 2+)
**Priority**: Advanced analytics and statistical validation
* **Date range filters in Analytics** - Flexible time period analysis (~4-5 hours)
  - Add date range selector (last 7/30/90 days, custom range)
  - Filter all analytics by selected period
  - Compare periods (e.g., "This month vs last month")
  - Show data availability per period
* **Charts & graphs visualization** - Make analytics more intuitive (~10-12 hours)
  - Line chart for symptom intensity over time
  - Bar chart for trigger frequencies
  - Pie chart for symptom type distribution
  - Use AndroidX Compose Charts or MPAndroidChart library
  - Interactive charts with zoom/pan
* **Phase 2: Statistical Enhancements** - Add statistical rigor to Phase 1 (~3-5 days)
  - Chi-square test for trigger significance (p-value < 0.05)
  - Confidence intervals for trigger percentages
  - Control for confounding variables
  - Bayesian approach for low-data scenarios
  - Multiple hypothesis correction (Bonferroni)
* **Phase 3: Advanced Pattern Recognition** (~7-10 days)
  - Time-of-day analysis (morning vs evening triggers)
  - Dose-response relationships (quantity matters)
  - Temporal patterns (weekday vs weekend)
  - Cumulative effects (multiple meals)
  - Symptom clustering
* **Phase 4: Personalization & Recommendations** (~15-20 days)
  - Personalized safe food suggestions
  - Meal planning recommendations
  - Trigger avoidance strategies
  - Export reports for healthcare providers

### 🔧 Technical Improvements
**Priority**: Code quality and consistency
* **Swipe gestures for edit/delete** - Implement intuitive gestures across all lists (~4-5 hours)
  - Swipe left reveals edit action
  - Swipe right reveals delete with confirmation
  - Apply to food items, symptoms, and future medication lists
  - Follow email app patterns for familiarity
* **Extract shared composables** - Reduce code duplication (~3-4 hours)
  - Create shared `EditDeleteActions` component
  - Reuse dialog layouts across screens
  - Shared loading state composables
  - Common empty state components
* **StateFlow lifecycle awareness audit** - Prevent memory leaks (~2 hours)
  - Audit all `collectAsState` calls
  - Ensure proper lifecycle-aware collection
  - Add lifecycle scoping where missing
  - Document best practices
* **Database size management** - Archive old data (~3-4 hours)
  - Add settings option for auto-archive (e.g., data older than 12 months)
  - Move archived data to separate table
  - Ability to view/restore archived data
  - Export before archive option
* **Analytics engine optimization** - Improve performance with large datasets (~8-10 hours)
  - Cache analytics results with timestamp
  - Incremental updates instead of full recalculation
  - Background processing with WorkManager
  - Show cached results while recalculating

---

## 📋 Development Roadmap

### ✅ Phase 1: Analytics Foundation (v1.11.0) - COMPLETED
✅ 3-hour symptom window (clinical standard)
✅ 30-minute meal grouping
✅ Meal-level PRIMARY analysis
✅ Individual food SECONDARY analysis
✅ Symptom-centric analysis with probability correlations
✅ Enhanced pattern detection with temporal and severity analysis
✅ Confidence indicators
✅ Co-occurrence tracking
✅ Collapsible UI cards

### ✅ Code Quality (v1.11.1) - COMPLETED
✅ Eliminated all 13 deprecation compilation warnings
✅ Migrated to modern Android/Compose APIs
✅ Edge-to-edge display implementation
✅ Zero behavioral changes, 100% test coverage maintained

### ✅ Critical Bug Fix (v1.12.0) - COMPLETED
✅ Fixed custom food persistence bug (PR #9)
✅ Custom foods now appear immediately in category lists
✅ Search functionality includes custom foods
✅ 30 comprehensive automated tests (100% passing)
✅ Performance validated (< 500ms category, < 1s search)
✅ No database migration required
✅ Backward compatible

### ✅ Cloud Backup Improvements (v1.13.1-v1.13.2) - COMPLETED
✅ Implemented cloud backup deletion with Drive API (v1.13.1)
✅ Comprehensive logging throughout deletion flow for debugging
✅ Fixed auto-backup storage bloat issue (v1.13.2)
✅ Auto-backups use fixed filenames that overwrite previous versions
✅ Manual backups use timestamped filenames (preserves all)
✅ Consistent behavior between local and cloud backups
✅ Prevents cloud storage accumulation from daily scheduled syncs

### 🎯 Next Release Options (v1.14.0)

**Option A: Remaining Critical Bugs** - 1-2 days (RECOMMENDED)
- Add error handling in ViewModels (UiState pattern)
- Implement input validation
- Enhance sync status indicator (last backup time, pending/failed states)

**Option B: Analytics Phase 2 (Statistical Enhancements)** - 3-5 days
- Build on Phase 1 foundation
- Add statistical significance testing
- Confidence intervals
- Better handling of low-data scenarios
- More accurate trigger identification

**Option C: Medication Tracking** - 3-5 days
- New tracking category
- Medication-symptom correlations
- Pre-populated medication database
- Integration with existing analytics

**Option D: UI/UX Overhaul** - 5-7 days
- Global Material Design 3 alignment
- Refactor symptom page
- Implement swipe gestures
- Category ordering by frequency

### Future Releases (v1.15.0+)
- Analytics Phase 3: Advanced Pattern Recognition
- Analytics Phase 4: Personalization & Recommendations
- Body weight history tracking
- Photo attachments for meals
- Accessibility improvements (font scaling, high contrast)
- Pagination for large lists (Paging3)
- ML-based predictions and forecasting

---

## 📝 Notes from Comprehensive Codebase Analysis

### Key Findings Summary:
This TODO.md has been enhanced based on a thorough codebase analysis that identified:

**Critical gaps** (now in 🔴 Critical section):
- Missing error handling and user feedback
- No input validation
- Sync status visibility needed

**Quick wins added** (now in ⚡ Quick Wins section):
- Skip quick-add confirmation toggle
- Loading states throughout app
- Reusable DateTimePicker component
- Database performance indexes
- Crash reporting setup

**New feature opportunities** (now in ⭐ New Features section):
- Bulk operations for batch actions
- Notes field on entries for context
- Export to PDF/CSV for healthcare sharing
- Reminders & notifications for consistency
- Undo functionality for safety

**UX improvements** (now in 🎨 UI/UX Polish section):
- Dashboard timeline enhancements (filters, search)
- Onboarding tutorial for new users
- Empty state improvements
- Dark mode toggle

**Analytics enhancements** (now in 📊 Analytics section):
- Date range filters
- Charts & graphs visualization
- Time estimates added to all phases

**Technical debt** (now in 🔧 Technical Improvements section):
- Code duplication reduction
- Memory leak prevention
- Database size management
- Analytics performance optimization

All items include time estimates and clear implementation details.