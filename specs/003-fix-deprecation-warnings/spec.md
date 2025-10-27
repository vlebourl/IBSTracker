# Feature Specification: Fix Deprecation Compilation Warnings

**Feature Branch**: `003-fix-deprecation-warnings`
**Created**: 2025-01-27
**Status**: Draft
**Input**: User description: "fix deprecation compilation warnings"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Clean Build Output (Priority: P1)

As a developer working on the IBS Tracker codebase, I need the project to compile without deprecation warnings so that I can identify real issues and maintain code quality without noise in the build output.

**Why this priority**: Clean build output is critical for developer productivity and code maintainability. Deprecation warnings hide actual issues and indicate the codebase is using outdated APIs that may be removed in future Android/Kotlin versions, creating technical debt.

**Independent Test**: Can be fully tested by running a clean build and verifying zero deprecation warnings appear in the compilation output. Delivers immediate value by ensuring all code uses current, non-deprecated APIs.

**Acceptance Scenarios**:

1. **Given** the project codebase, **When** developer runs `./gradlew compileDebugKotlin --warning-mode all`, **Then** zero deprecation warnings appear in the output
2. **Given** the project codebase, **When** developer runs `./gradlew compileReleaseKotlin --warning-mode all`, **Then** zero deprecation warnings appear in the output
3. **Given** any Kotlin source file, **When** developer opens it in Android Studio, **Then** no yellow/orange deprecation highlighting appears

---

### User Story 2 - Future-Proof Codebase (Priority: P2)

As a maintainer of the IBS Tracker app, I need all deprecated APIs replaced with their modern equivalents so that the app remains compatible with future Android SDK and Compose library updates.

**Why this priority**: Using deprecated APIs creates risk for future updates. When deprecated APIs are removed in newer library versions, the app may fail to compile or exhibit runtime issues.

**Independent Test**: Can be tested by attempting to upgrade to the latest stable versions of all dependencies and verifying the app still compiles and functions correctly.

**Acceptance Scenarios**:

1. **Given** the codebase uses deprecated Material Icons, **When** developer inspects icon usage, **Then** all icons use the AutoMirrored versions where applicable
2. **Given** the codebase uses deprecated Compose components, **When** developer reviews Compose code, **Then** all components use the latest recommended APIs (e.g., `SecondaryIndicator` instead of `Indicator`, lambda-based `LinearProgressIndicator`)
3. **Given** the codebase uses deprecated Android APIs, **When** developer reviews Android framework usage, **Then** all APIs use the recommended modern alternatives

---

### User Story 3 - Improved Code Quality Metrics (Priority: P3)

As a project stakeholder, I need the codebase to follow current best practices so that static analysis tools report high code quality scores and the project meets modern development standards.

**Why this priority**: Code quality metrics matter for long-term maintainability and developer confidence. While less urgent than functional issues, addressing deprecation warnings demonstrates professional code stewardship.

**Independent Test**: Can be tested by running static analysis tools (Android Lint, Detekt) and verifying improved scores with zero deprecation-related warnings.

**Acceptance Scenarios**:

1. **Given** static analysis tools scan the codebase, **When** analysis completes, **Then** no deprecation-related warnings appear in the report
2. **Given** the project README or documentation, **When** reviewed, **Then** it can state "zero compilation warnings" as a quality metric
3. **Given** new developers joining the project, **When** they review the codebase, **Then** they see modern, up-to-date code practices

---

### Edge Cases

- What happens when a deprecated API has no direct replacement? (Document workaround or migration path)
- How does the system handle deprecation warnings in generated code (e.g., Room DAOs)? (Verify generated code uses current APIs)
- What if replacing a deprecated API changes behavior subtly? (Add tests to verify equivalent behavior)
- How are deprecation warnings in third-party dependencies handled? (Document as external dependencies, track for future updates)

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: All deprecated Material Icons MUST be replaced with their AutoMirrored equivalents (Icons.AutoMirrored.Filled.*)
- **FR-002**: All deprecated Compose UI components MUST be replaced with their recommended modern equivalents
- **FR-003**: All deprecated Android framework APIs MUST be replaced with their recommended modern alternatives
- **FR-004**: The build process MUST complete with zero deprecation warnings in Kotlin compilation
- **FR-005**: All code changes MUST maintain existing functionality without behavioral changes
- **FR-006**: Any unavoidable deprecation warnings MUST be documented with suppression rationale and tracking for future resolution
- **FR-007**: Build configuration MUST use `--warning-mode all` to surface all deprecation warnings during development

### Key Entities *(include if feature involves data)*

This is a code quality feature that does not involve data entities. All changes are to existing code structure and API usage.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Build output shows zero deprecation warnings when running `./gradlew compileDebugKotlin --warning-mode all`
- **SC-002**: Build output shows zero deprecation warnings when running `./gradlew compileReleaseKotlin --warning-mode all`
- **SC-003**: All existing tests continue to pass after deprecation fixes (100% test pass rate maintained)
- **SC-004**: Android Studio IDE shows zero deprecation highlighting (yellow/orange) across all Kotlin source files
- **SC-005**: Static analysis reports improve by eliminating all deprecation-related warnings

## Context

### Current Deprecation Warnings

The codebase currently has **13 deprecation warnings** across multiple files:

**Material Icons (AutoMirrored)** - 6 warnings:
- PatternsTab.kt: Icons.Filled.TrendingUp (2 occurrences)
- AnalysisScreen.kt: Icons.Filled.Help, Icons.Filled.HelpOutline, Icons.Filled.ArrowBack, Icons.Filled.ArrowForward

**Compose UI Components** - 2 warnings:
- AnalysisScreen.kt: Indicator component (use SecondaryIndicator instead)
- AnalysisScreen.kt: LinearProgressIndicator with Float progress (use lambda overload)

**Android Framework APIs** - 2 warnings:
- Theme.kt: statusBarColor property
- LocaleHelper.kt: updateConfiguration() method

**Code Quality** - 2 warnings:
- CredentialManagerAuth.kt: Condition is always 'true'
- ViewModelFactory.kt: Unchecked cast warning

**App-Specific Deprecation** - 1 warning:
- CommonFoods.kt: getCommonFoods() function (use CommonFoodDao.getCommonFoodsByCategory())

### Assumptions

- All deprecated APIs have documented modern replacements in official Android/Compose documentation
- Replacing deprecated APIs will not introduce breaking changes to existing functionality
- The team has access to Android Studio with updated SDK and Compose libraries
- Unit and integration tests exist to verify functionality remains intact after changes
- Build pipeline enforces warnings to prevent new deprecations from being introduced

## Dependencies

- Android SDK 34 (latest stable as of specification)
- Jetpack Compose BOM 2023.08.00 or later
- Kotlin 1.8.20 or later
- Material Icons Extended library
- Existing test suite for regression verification

## Out of Scope

- Updating third-party library dependencies (unless required for deprecation fixes)
- Refactoring code beyond what's necessary to fix deprecation warnings
- Adding new features or functionality
- Performance optimization (unless directly related to API replacement)
- Changing UI/UX behavior (replacements must maintain visual/functional parity)

## Notes

This is a technical debt and code quality feature focused on modernizing API usage. All changes should be non-breaking and maintain existing behavior while eliminating deprecation warnings from the build output.
