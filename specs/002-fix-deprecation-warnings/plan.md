# Implementation Plan: Fix Build Process Deprecation Warnings

**Branch**: `002-fix-deprecation-warnings` | **Date**: 2025-10-24 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-fix-deprecation-warnings/spec.md`

## Summary

This implementation addresses all deprecation warnings in the Android build process by migrating from deprecated APIs to their current replacements across 5 categories: FoodCategory display name retrieval (15+ occurrences), Compose UI components (9 occurrences), KeyboardOptions constructor (4 occurrences), Google Drive AndroidHttp (2 occurrences), and CommonFoods helper (1 occurrence). The approach ensures zero behavioral changes while achieving full build compatibility with current library versions.

**Key Technical Approach**: Incremental API migration with comprehensive testing after each category, prioritized by occurrence count and impact scope.

## Technical Context

**Language/Version**: Kotlin 21 (JVM target), Android Gradle Plugin
**Primary Dependencies**:
- Jetpack Compose BOM 2023.08.00 (Material3, Foundation, Navigation)
- Room 2.6.1 (KSP annotation processing)
- Google Drive API (current version from project)

**Storage**: Room SQLite database (no schema changes required)
**Testing**:
- JUnit 4.13.2 for unit tests
- AndroidX Test (JUnit 1.1.5, Espresso 3.5.1) for instrumented tests
- Existing test suite must pass after each migration

**Target Platform**: Android 7.0+ (minSdk 26), targetSdk 34
**Project Type**: Single Android mobile application with Jetpack Compose UI
**Performance Goals**: Build time within 5% of baseline, no runtime performance regression
**Constraints**:
- Zero behavioral changes to user-facing functionality
- Visual parity required for all UI component changes
- Internationalization must continue working correctly

**Scale/Scope**: ~30 deprecation warnings across 8 source files (5 screens + 1 data model + 1 sync module + 1 database file)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Status**: ✅ PASS (No project constitution defined - proceeding with standard Android/Kotlin best practices)

Since no project-specific constitution exists at `.specify/memory/constitution.md`, this implementation will follow:
- Android API Guidelines for deprecated API migration
- Material Design 3 component migration best practices
- Jetpack Compose API evolution guidelines
- Backward compatibility requirements for production apps

**Re-evaluation after Phase 1**: Not applicable (no constitution to validate against)

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

Not applicable - no constitution violations.

## Project Structure

### Documentation (this feature)

```text
specs/002-fix-deprecation-warnings/
├── spec.md              # Feature specification
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command) - N/A for refactoring
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command) - N/A for refactoring
├── checklists/
│   └── requirements.md  # Specification quality checklist (already completed)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
app/src/main/java/com/tiarkaerell/ibstracker/
├── data/
│   ├── model/
│   │   ├── CommonFoods.kt          # P5: Remove deprecated getCommonFoods()
│   │   └── FoodCategory.kt         # P1: Update getDisplayName() usage references
│   ├── database/
│   │   └── dao/                    # P5: Use CommonFoodDao instead of helper
│   └── sync/
│       └── GoogleDriveBackup.kt    # P4: Replace AndroidHttp transport
├── ui/
│   ├── screens/
│   │   ├── AnalyticsScreen.kt      # P1: Replace FoodCategory.getDisplayName (2 occurrences)
│   │   ├── DashboardScreen.kt      # P1: Replace FoodCategory.getDisplayName (6 occurrences)
│   │   │                           # P2: Replace Divider (2), menuAnchor() (1)
│   │   ├── FoodScreen.kt           # P1: Replace FoodCategory.getDisplayName (7 occurrences)
│   │   └── SettingsScreen.kt       # P2: Replace Divider (3), menuAnchor() (3)
│   │                               # P3: Replace KeyboardOptions constructor (4)
│   └── theme/                      # Reference Material3 theme configuration
└── tests/
    ├── androidTest/                # Instrumented tests (must continue passing)
    └── test/                       # Unit tests (must continue passing)
```

**Structure Decision**: Standard Android single-module application with clean architecture (data/ui separation). All deprecation fixes are isolated to existing files - no new files or architectural changes required. Migration follows existing code organization patterns.

## Phase 0: Research & Discovery

### Research Tasks

**Note**: This is a well-documented API migration task with clear replacement paths provided by the Android/Compose deprecation messages. Research focuses on verifying migration patterns and identifying any behavioral differences.

#### Task 0.1: FoodCategory Display Name Migration Pattern

**Research Question**: How should `FoodCategory.getDisplayName(context, category)` be replaced to maintain internationalization behavior?

**Investigation Approach**:
1. Read current FoodCategory implementation to understand getDisplayName() logic
2. Verify displayName and displayNameFr properties provide equivalent functionality
3. Confirm locale selection mechanism works with direct property access
4. Document any context-dependent behavior that needs preservation

**Expected Outcome**: Clear migration pattern (e.g., `category.displayName` for English, `category.displayNameFr` for French, or locale-aware selection logic)

#### Task 0.2: Compose Material3 Component Migration

**Research Question**: What are the exact replacements for Divider and menuAnchor() to maintain visual and functional parity?

**Investigation Approach**:
1. Consult Compose Material3 migration guide for Divider → HorizontalDivider
2. Check menuAnchor() signature changes - what are the required parameters (MenuAnchorType, enabled)?
3. Verify default parameter values maintain current behavior
4. Document any visual or behavioral differences (spacing, positioning, etc.)

**Expected Outcome**:
- HorizontalDivider usage pattern with equivalent styling
- menuAnchor(MenuAnchorType, enabled) signature with appropriate defaults

#### Task 0.3: KeyboardOptions Constructor Migration

**Research Question**: What is the difference between `autoCorrect` Boolean and `autoCorrectEnabled` optional parameter?

**Investigation Approach**:
1. Review Compose Foundation release notes for KeyboardOptions changes
2. Understand parameter mapping: autoCorrect=true/false → autoCorrectEnabled=true/false/null
3. Identify if null (default) has different behavior than explicit true/false
4. Document any user-visible autocorrect behavior differences

**Expected Outcome**: Clear parameter migration pattern (likely direct mapping: autoCorrect → autoCorrectEnabled)

#### Task 0.4: Google Drive API Transport Migration

**Research Question**: What is the current replacement for deprecated AndroidHttp in Google Drive API?

**Investigation Approach**:
1. Check Google Drive API library version currently in use (from build.gradle)
2. Review Google Drive API migration documentation for AndroidHttp alternatives
3. Identify recommended transport (likely NetHttpTransport or OkHttp-based transport)
4. Verify authentication compatibility and network configuration requirements

**Expected Outcome**: Specific transport class to use (e.g., NetHttpTransport.Builder()) and initialization pattern

#### Task 0.5: CommonFoods DAO Migration Pattern

**Research Question**: How should deprecated `CommonFoods.getCommonFoods()` calls be replaced with DAO access?

**Investigation Approach**:
1. Read CommonFoods.kt to locate deprecated function and its current implementation
2. Verify CommonFoodDao.getCommonFoodsByCategory() provides equivalent functionality
3. Identify call sites and required context changes (Repository access pattern)
4. Document any threading or Flow conversion requirements

**Expected Outcome**: Clear migration pattern showing DAO access through repository with proper coroutine scope

### Research Consolidation Format

All findings will be documented in `research.md` with this structure for each task:

```markdown
### [Research Task Title]

**Decision**: [Chosen replacement API and pattern]

**Rationale**: [Why this approach maintains parity and follows best practices]

**Alternatives Considered**: [Other options evaluated and why rejected]

**Migration Pattern**:
```kotlin
// Before (deprecated)
[old code example]

// After (current)
[new code example]
```

**Behavioral Notes**: [Any subtle differences or considerations]
```

## Phase 1: Design & Contracts

**Prerequisites:** `research.md` complete with all 5 research tasks resolved

### Phase 1 Outputs

#### 1.1 Data Model (`data-model.md`)

**Status**: NOT APPLICABLE for this feature

This is a pure API migration task with no data model changes. The existing Room database schema, entities, and DAOs remain unchanged. The only data-related change is replacing a deprecated helper function with direct DAO access, which doesn't alter the data model itself.

**Rationale**: Deprecation fixes are purely implementation changes. No new entities, no schema migrations, no data relationships modified.

#### 1.2 API Contracts (`contracts/`)

**Status**: NOT APPLICABLE for this feature

This is an internal refactoring task with no API contract changes. The application's public surface (user interactions, data persistence) remains identical. All changes are internal API usage updates (Android framework APIs, Compose APIs, Google Drive APIs).

**Rationale**:
- No new REST/GraphQL endpoints
- No new public interfaces or services
- No changes to existing API signatures (except replacing deprecated internals)
- User-facing contract (UI behavior, data storage) unchanged

#### 1.3 Migration Quickstart (`quickstart.md`)

This document will provide a step-by-step guide for developers to execute the deprecation fixes, including testing verification at each step.

**Structure**:
```markdown
# Deprecation Fix Migration Guide

## Prerequisites
- Branch: 002-fix-deprecation-warnings checked out
- Baseline build: ./gradlew clean build --warning-mode all (capture warning count)
- All tests passing: ./gradlew test connectedAndroidTest

## Migration Order (by Priority)

### Step 1: FoodCategory Display Name (P1 - 15+ occurrences)
[Detailed find/replace patterns for each affected file]

### Step 2: Compose UI Components (P2 - 9 occurrences)
[Divider → HorizontalDivider and menuAnchor() updates]

### Step 3: KeyboardOptions Constructor (P3 - 4 occurrences)
[Parameter migration pattern]

### Step 4: Google Drive AndroidHttp (P4 - 2 occurrences)
[Transport replacement pattern]

### Step 5: CommonFoods Helper (P5 - 1 occurrence)
[DAO access pattern]

## Testing Protocol
[After each step: compile, run affected tests, verify UI screens]

## Rollback Procedure
[Git revert strategy if issues discovered]
```

#### 1.4 Agent Context Update

After completing Phase 1 design artifacts, run the agent context update script:

```bash
.specify/scripts/bash/update-agent-context.sh claude
```

This will update `.specify/memory/agent-file-template.md` with any new technology patterns discovered during research (e.g., specific Material3 migration patterns, Google Drive transport configurations).

**Expected Additions**:
- Material3 HorizontalDivider usage pattern
- Compose menuAnchor with MenuAnchorType parameter
- KeyboardOptions autoCorrectEnabled parameter
- Google Drive API current transport mechanism
- Room DAO access patterns (if not already documented)

## Phase 2: Task Breakdown

**NOT EXECUTED IN THIS COMMAND** - Task breakdown is handled by `/speckit.tasks` command.

The tasks.md file will be generated based on this plan and will include:
1. Atomic tasks for each deprecation category (P1-P5)
2. Testing verification tasks after each migration
3. Final validation task (zero deprecation warnings in full build)
4. Documentation update tasks

**Estimated Task Count**: 12-15 tasks
- 5 migration tasks (one per priority category)
- 5 testing verification tasks
- 1 baseline measurement task
- 1 final validation task
- 1-2 documentation tasks

## Risk Mitigation Strategies

### Technical Risks

1. **Internationalization Breaking**
   - **Mitigation**: Test language switching thoroughly after P1 FoodCategory migration
   - **Validation**: Manual testing with device language set to French/English
   - **Rollback Trigger**: Any language-specific display issues

2. **UI Rendering Differences**
   - **Mitigation**: Visual comparison screenshots before/after P2 Compose migration
   - **Validation**: Run app on emulator, capture screenshots of affected screens
   - **Rollback Trigger**: Any visible layout or styling changes

3. **Google Drive Sync Failure**
   - **Mitigation**: Test backup/restore with real Google Drive account after P4 migration
   - **Validation**: Execute full backup cycle, verify data integrity
   - **Rollback Trigger**: Any authentication or data transfer errors

4. **Test Suite Regressions**
   - **Mitigation**: Run full test suite after each priority category migration
   - **Validation**: ./gradlew test connectedAndroidTest must pass
   - **Rollback Trigger**: Any test failures that can't be resolved immediately

### Process Risks

1. **Incremental Changes Lost**
   - **Mitigation**: Commit after each priority category successfully migrated and tested
   - **Strategy**: P1 commit → P2 commit → P3 commit → P4 commit → P5 commit
   - **Benefit**: Easy rollback to last working state if issues discovered

2. **Incomplete Migration**
   - **Mitigation**: Final validation task verifies zero deprecation warnings
   - **Strategy**: ./gradlew clean build --warning-mode all as final gate
   - **Success Criteria**: Must show 0 deprecation warnings before marking complete

## Success Criteria Validation

The implementation will be considered complete when:

1. ✅ `./gradlew clean build --warning-mode all` produces zero deprecation warnings (SC-001)
2. ✅ All 31+ specific occurrences replaced across all categories (SC-002 through SC-007)
3. ✅ Build time within 5% of baseline (SC-008)
4. ✅ All existing automated tests pass (SC-009)
5. ✅ Visual regression testing shows no UI differences (SC-010)

**Validation Commands**:
```bash
# Success Criteria 1: Zero warnings
./gradlew clean build --warning-mode all 2>&1 | grep -i "deprecat" | wc -l
# Expected: 0

# Success Criteria 2-7: Count replacements via git diff
git diff main...002-fix-deprecation-warnings --stat

# Success Criteria 8: Build time comparison
time ./gradlew clean build  # Compare to baseline

# Success Criteria 9: Test suite
./gradlew test connectedAndroidTest
# Expected: All tests pass

# Success Criteria 10: Visual testing (manual)
# Launch app, navigate through affected screens, compare to baseline screenshots
```

## Next Steps

After this command completes:

1. ✅ Review generated `research.md` - verify all NEEDS CLARIFICATION resolved
2. ✅ Review generated `quickstart.md` - understand migration steps
3. ⏭️ Run `/speckit.tasks` to generate atomic task breakdown
4. ⏭️ Begin implementation following task order (P1 → P2 → P3 → P4 → P5)
5. ⏭️ Execute testing protocol after each category migration
6. ⏭️ Final validation before merging to main branch

---

**Plan Status**: Ready for Phase 0 Research Execution
**Generated**: 2025-10-24
**Next Command**: Research task execution (within this command) → Generate research.md
