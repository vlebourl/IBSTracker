# Feature Specification: Fix Build Process Deprecation Warnings

**Feature Branch**: `002-fix-deprecation-warnings`
**Created**: 2025-10-24
**Status**: Draft
**Input**: User description: "fix deprecation warnings in the build process."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Update Deprecated FoodCategory Display Name Usage (Priority: P1)

Developers and build automation systems need to build the application without encountering deprecation warnings related to food category display name retrieval. Currently, the code uses deprecated `FoodCategory.getDisplayName(context, category)` method across multiple screens, which needs to be replaced with direct property access.

**Why this priority**: This is the most widespread deprecation issue (appearing in 15+ locations across AnalyticsScreen, DashboardScreen, FoodScreen), and impacts the most critical user-facing screens. Resolving this provides the greatest immediate value for build cleanliness.

**Independent Test**: Can be fully tested by running a full build with `--warning-mode all` and verifying no deprecation warnings appear for FoodCategory.getDisplayName calls, and delivers a cleaner build output with fewer warnings.

**Acceptance Scenarios**:

1. **Given** the codebase uses `FoodCategory.getDisplayName(context, category)` in AnalyticsScreen, **When** developer runs build with warning mode enabled, **Then** no deprecation warnings appear for FoodCategory display name retrieval in AnalyticsScreen
2. **Given** the codebase uses `FoodCategory.getDisplayName(context, category)` in DashboardScreen, **When** developer runs build with warning mode enabled, **Then** no deprecation warnings appear for FoodCategory display name retrieval in DashboardScreen
3. **Given** the codebase uses `FoodCategory.getDisplayName(context, category)` in FoodScreen, **When** developer runs build with warning mode enabled, **Then** no deprecation warnings appear for FoodCategory display name retrieval in FoodScreen

---

### User Story 2 - Replace Deprecated Compose UI Components (Priority: P2)

Developers need to build the application using current Jetpack Compose APIs instead of deprecated UI components. Several screens use deprecated `Divider` component and deprecated `menuAnchor()` modifier signature that need updating.

**Why this priority**: While less widespread than the FoodCategory issue, these deprecated Compose APIs affect UI components and could impact future Compose library updates. Addressing these ensures compatibility with newer Compose versions.

**Independent Test**: Can be fully tested by running a full build and verifying no deprecation warnings for Divider or menuAnchor components, and delivers a codebase compatible with current Compose Material3 APIs.

**Acceptance Scenarios**:

1. **Given** DashboardScreen uses deprecated `Divider` component, **When** developer runs build with warning mode enabled, **Then** no deprecation warnings appear for Divider usage and UI renders correctly with HorizontalDivider
2. **Given** SettingsScreen uses deprecated `Divider` component, **When** developer runs build with warning mode enabled, **Then** no deprecation warnings appear for Divider usage and UI renders correctly with HorizontalDivider
3. **Given** DashboardScreen and SettingsScreen use deprecated `menuAnchor()` without parameters, **When** developer runs build with warning mode enabled, **Then** no deprecation warnings appear for menuAnchor usage and dropdown menus function correctly

---

### User Story 3 - Update KeyboardOptions Constructor Usage (Priority: P3)

Developers need to use the current KeyboardOptions constructor API in text input fields. The deprecated constructor with `autoCorrect` Boolean parameter needs to be replaced with the new constructor using optional `autoCorrectEnabled` parameter.

**Why this priority**: This affects only SettingsScreen and is limited in scope (4 occurrences). While important for API currency, it has lower impact than the more widespread issues.

**Independent Test**: Can be fully tested by running a full build and verifying no deprecation warnings for KeyboardOptions constructor, and delivers text input fields that use current Compose Foundation APIs.

**Acceptance Scenarios**:

1. **Given** SettingsScreen uses deprecated KeyboardOptions constructor with autoCorrect parameter, **When** developer runs build with warning mode enabled, **Then** no deprecation warnings appear for KeyboardOptions constructor usage
2. **Given** text input fields in SettingsScreen use updated KeyboardOptions, **When** user interacts with text fields, **Then** autocorrect behavior remains unchanged and functions as expected

---

### User Story 4 - Update Google Drive AndroidHttp Usage (Priority: P4)

Developers need to use current Google Drive API transport mechanisms instead of deprecated AndroidHttp class. The GoogleDriveBackup module uses deprecated AndroidHttp for network transport.

**Why this priority**: While technically important, this affects only backup/sync functionality which is not critical for core app functionality. It's isolated to one module and has lower immediate impact.

**Independent Test**: Can be fully tested by running a full build and verifying no deprecation warnings for AndroidHttp, and delivers Google Drive backup functionality that uses current transport APIs.

**Acceptance Scenarios**:

1. **Given** GoogleDriveBackup uses deprecated AndroidHttp class, **When** developer runs build with warning mode enabled, **Then** no deprecation warnings appear for AndroidHttp usage
2. **Given** GoogleDriveBackup uses updated transport mechanism, **When** user performs backup/restore operation, **Then** Google Drive sync functions correctly without errors

---

### User Story 5 - Remove Deprecated CommonFoods Helper Usage (Priority: P5)

Developers need to use direct DAO access for common foods instead of deprecated helper functions. The CommonFoods helper contains deprecated `getCommonFoods()` method that should be replaced with direct database access.

**Why this priority**: This is the lowest priority as it appears to be internal code used during migration/refactoring, and the alternative (CommonFoodDao) already exists and is recommended.

**Independent Test**: Can be fully tested by running a full build and verifying no deprecation warnings for CommonFoods.getCommonFoods(), and delivers food data retrieval using the recommended DAO pattern.

**Acceptance Scenarios**:

1. **Given** code uses deprecated `CommonFoods.getCommonFoods()` method, **When** developer runs build with warning mode enabled, **Then** no deprecation warnings appear for CommonFoods helper usage
2. **Given** code uses `CommonFoodDao.getCommonFoodsByCategory()` instead, **When** app retrieves common foods, **Then** food data is retrieved correctly from database

---

### Edge Cases

- What happens when deprecated API has no exact equivalent in new API (may require behavior adaptation)?
- How does the system handle cases where deprecated parameters had specific behavior that needs to be preserved?
- What happens when multiple deprecated patterns exist in the same code block (order of refactoring)?
- How do we ensure internationalization still works after updating FoodCategory display name access pattern?
- What happens if menuAnchor parameter requirements differ between versions?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Build system MUST compile the entire codebase without any deprecation warnings when using `--warning-mode all`
- **FR-002**: All deprecated `FoodCategory.getDisplayName(context, category)` calls MUST be replaced with direct property access (category.displayName or category.displayNameFr)
- **FR-003**: All deprecated `Divider` component usages MUST be replaced with `HorizontalDivider` component while maintaining identical visual appearance
- **FR-004**: All deprecated `menuAnchor()` modifier calls MUST be updated to use the overload that takes MenuAnchorType and enabled parameters
- **FR-005**: All deprecated `KeyboardOptions` constructor calls with `autoCorrect` Boolean parameter MUST be updated to use the new constructor with `autoCorrectEnabled` parameter
- **FR-006**: GoogleDriveBackup MUST replace deprecated `AndroidHttp` class with current Google Drive API transport mechanism
- **FR-007**: All deprecated `CommonFoods.getCommonFoods()` calls MUST be replaced with `CommonFoodDao.getCommonFoodsByCategory()`
- **FR-008**: All changes MUST preserve existing application behavior and functionality exactly
- **FR-009**: All UI components MUST render identically before and after deprecation fixes
- **FR-010**: All text input behavior MUST remain unchanged after KeyboardOptions updates
- **FR-011**: Google Drive backup/restore functionality MUST work identically after transport updates
- **FR-012**: Internationalization support for food categories MUST continue to work correctly after display name updates

### Key Entities

- **Deprecation Warning**: A compiler/build tool notification indicating use of an API marked for future removal, includes location (file:line), deprecated API name, and recommended replacement
- **Build Configuration**: Settings that control warning visibility and build behavior, includes warning mode (all/summary/none) and deprecation handling policies
- **API Migration Path**: The documented transition from deprecated API to current API, includes replacement API name, parameter mappings, and behavioral changes

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Full build with `./gradlew clean build --warning-mode all` produces zero deprecation warnings
- **SC-002**: All 15+ occurrences of deprecated FoodCategory.getDisplayName are successfully replaced
- **SC-003**: All 5 occurrences of deprecated Divider component are replaced with HorizontalDivider
- **SC-004**: All 4 occurrences of deprecated menuAnchor() are updated with current signature
- **SC-005**: All 4 occurrences of deprecated KeyboardOptions constructor are updated
- **SC-006**: All 2 occurrences of deprecated AndroidHttp are replaced with current transport
- **SC-007**: All 1 occurrence of deprecated CommonFoods.getCommonFoods is replaced with DAO access
- **SC-008**: Build time remains within 5% of previous build time (no significant performance regression)
- **SC-009**: All existing automated tests continue to pass after changes
- **SC-010**: Visual regression testing shows no UI differences in affected screens

## Scope *(mandatory)*

### In Scope

- Replacing all deprecated API calls identified in build warnings
- Updating FoodCategory display name retrieval patterns across all screens
- Modernizing Compose Material3 component usage (Divider → HorizontalDivider)
- Updating Compose menuAnchor modifier signatures
- Updating KeyboardOptions constructor usage
- Replacing deprecated Google Drive AndroidHttp transport
- Removing deprecated CommonFoods helper function usage
- Ensuring visual and functional parity after changes
- Documenting migration patterns for future reference

### Out of Scope

- Adding new features or functionality
- Refactoring code beyond what's necessary to fix deprecations
- Upgrading library versions (unless required to fix specific deprecation)
- Changing UI design or layout beyond maintaining identical appearance
- Modifying business logic or data processing
- Performance optimization unrelated to deprecation fixes
- Adding new tests (existing tests must continue to pass)
- Internationalization improvements (must maintain current i18n support)

## Assumptions *(mandatory)*

1. All deprecated APIs have documented replacement APIs available in current library versions
2. Current library versions in the project support both deprecated and new APIs (migration can happen incrementally)
3. Existing automated tests provide sufficient coverage to catch behavioral regressions
4. No breaking changes exist between deprecated and replacement APIs that would require architectural changes
5. Build system is configured correctly and deprecation warnings accurately reflect code issues
6. Google Drive API library version in use provides a current alternative to AndroidHttp
7. All affected screens have been identified by the current build warnings scan
8. Visual appearance requirements can be validated through manual testing or existing screenshot tests
9. Internationalization for food categories works through direct property access (displayName/displayNameFr)
10. Menu behavior remains consistent with updated menuAnchor signature when using default parameters

## Dependencies *(mandatory)*

### Internal Dependencies

- Room database schema must remain stable during changes
- ViewModel layer must continue to provide same data contracts
- Existing UI components must remain compatible with updated Compose APIs
- Localization resources must support direct property access pattern for food categories

### External Dependencies

- Jetpack Compose BOM version must support both deprecated and replacement APIs during transition
- Jetpack Compose Material3 library must provide HorizontalDivider and updated menuAnchor
- Jetpack Compose Foundation library must provide updated KeyboardOptions constructor
- Google Drive API library must provide current transport mechanism as AndroidHttp replacement
- Kotlin compiler version must support updated API patterns

### Technical Dependencies

- Build system must support incremental compilation during API migration
- Gradle must correctly report deprecation warnings with `--warning-mode all`
- Android SDK must be compatible with updated Compose and Google Drive API versions

## Risks *(mandatory)*

### Technical Risks

- **Risk**: Subtle behavioral differences between deprecated and replacement APIs that aren't caught by tests
  **Mitigation**: Thorough manual testing of all affected UI screens; careful review of API documentation for behavioral notes; incremental changes with testing after each screen

- **Risk**: Internationalization breaking if display name property access differs from helper method behavior
  **Mitigation**: Test language switching thoroughly; review FoodCategory implementation to understand displayName vs displayNameFr selection logic

- **Risk**: Google Drive sync breaking due to transport mechanism changes
  **Mitigation**: Test backup/restore functionality thoroughly with real Google Drive account; review Google Drive API migration documentation; implement proper error handling

- **Risk**: UI rendering differences with HorizontalDivider vs Divider
  **Mitigation**: Visual inspection of all affected screens; compare spacing and styling before/after; verify Material3 theme compatibility

- **Risk**: Dropdown menu behavior changes with updated menuAnchor signature
  **Mitigation**: Test all dropdown menus in affected screens; verify positioning and interaction behavior; consult Compose documentation for default parameter behavior

### Business Risks

- **Risk**: Development time spent on deprecation fixes instead of new features
  **Mitigation**: This is technical debt that prevents future library updates; addressing now prevents larger migration costs later

- **Risk**: Regression bugs introduced during API migration affecting user experience
  **Mitigation**: Comprehensive testing before release; incremental rollout if possible; clear rollback plan

## Open Questions

None - all implementation details can be determined from build warnings and API documentation.
