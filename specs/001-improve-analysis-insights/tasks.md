# Tasks: Improved Analysis Insights

**Input**: Design documents from `/specs/001-improve-analysis-insights/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and Android-specific structure

- [x] T001 Create analysis package structure in app/src/main/java/com/tiarkaerell/ibstracker/data/analysis/
- [x] T002 Create analysis models package in app/src/main/java/com/tiarkaerell/ibstracker/data/model/
- [x] T003 [P] Create analysis UI components package in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/analysis/
- [x] T004 [P] Create analysis repository package in app/src/main/java/com/tiarkaerell/ibstracker/data/repository/
- [x] T005 [P] Create test packages in app/src/test/java/com/tiarkaerell/ibstracker/analysis/
- [x] T006 [P] Create Android UI test package in app/src/androidTest/java/com/tiarkaerell/ibstracker/ui/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core data models and analysis infrastructure that ALL user stories depend on

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T007 Create AnalysisResult data class in app/src/main/java/com/tiarkaerell/ibstracker/data/model/AnalysisResult.kt
- [x] T008 [P] Create SymptomAnalysis data class in app/src/main/java/com/tiarkaerell/ibstracker/data/model/SymptomAnalysis.kt
- [x] T009 [P] Create TriggerProbability data class in app/src/main/java/com/tiarkaerell/ibstracker/data/model/TriggerProbability.kt
- [x] T010 [P] Create CorrelationEvidence data class in app/src/main/java/com/tiarkaerell/ibstracker/data/model/CorrelationEvidence.kt
- [x] T011 [P] Create AnalysisTimeWindow data class in app/src/main/java/com/tiarkaerell/ibstracker/data/model/AnalysisTimeWindow.kt
- [x] T012 [P] Create AnalysisFilters data class in app/src/main/java/com/tiarkaerell/ibstracker/data/model/AnalysisFilters.kt
- [x] T013 [P] Create IBSTriggerCategory enum with baseline probabilities in app/src/main/java/com/tiarkaerell/ibstracker/data/model/TriggerProbability.kt
- [x] T014 [P] Create SeverityLevel and RecommendationLevel enums in app/src/main/java/com/tiarkaerell/ibstracker/data/model/SymptomAnalysis.kt
- [x] T015 Create CorrelationCalculator class in app/src/main/java/com/tiarkaerell/ibstracker/data/analysis/CorrelationCalculator.kt
- [x] T016 [P] Create ProbabilityEngine class in app/src/main/java/com/tiarkaerell/ibstracker/data/analysis/ProbabilityEngine.kt
- [x] T017 [P] Create FoodGroupMapper utility in app/src/main/java/com/tiarkaerell/ibstracker/utils/FoodGroupMapper.kt
- [x] T018 Create AnalysisRepository interface implementation in app/src/main/java/com/tiarkaerell/ibstracker/data/repository/AnalysisRepositoryImpl.kt
- [x] T019 Add correlation cache table to Room database - CorrelationCache entity created in app/src/main/java/com/tiarkaerell/ibstracker/data/model/CorrelationCache.kt
- [x] T020 Create correlation cache DAO in app/src/main/java/com/tiarkaerell/ibstracker/data/database/dao/CorrelationCacheDao.kt

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - View Symptom-Based Trigger Analysis (Priority: P1) 🎯 MVP

**Goal**: Display symptom-centric view showing food triggers with probability percentages for each symptom

**Independent Test**: Can be fully tested by entering several symptoms and food items, then viewing the analysis to see if correlations are clearly displayed.

### Core Analysis Engine for User Story 1

- [x] T021 [P] [US1] Create TriggerAnalyzer main orchestrator in app/src/main/java/com/tiarkaerell/ibstracker/data/analysis/TriggerAnalyzer.kt
- [x] T022 [P] [US1] Implement temporal proximity calculation in CorrelationCalculator.kt (exponential decay function)
- [x] T023 [P] [US1] Implement baseline trigger probability lookup in ProbabilityEngine.kt
- [x] T024 [P] [US1] Implement frequency correlation calculation in CorrelationCalculator.kt
- [x] T025 [US1] Implement weighted correlation formula combining temporal + baseline + frequency in CorrelationCalculator.kt
- [x] T026 [US1] Implement probability percentage calculation in ProbabilityEngine.kt
- [x] T027 [US1] Implement correlation confidence calculation in ProbabilityEngine.kt
- [x] T028 [US1] Implement main analysis generation method in AnalysisRepositoryImpl.kt

### Basic UI Components for User Story 1

- [x] T029 [P] [US1] Create SymptomCard composable integrated in AnalysisScreen.kt (SymptomAnalysisCard component)
- [x] T030 [P] [US1] Create TriggerProbabilityBar composable integrated in AnalysisScreen.kt (TriggerProbabilityBar component)
- [x] T031 [P] [US1] Create SeverityIndicator composable integrated in AnalysisScreen.kt (color-coded severity indicators)
- [x] T032 [US1] Create basic AnalysisScreen composable layout in app/src/main/java/com/tiarkaerell/ibstracker/ui/screens/AnalysisScreen.kt
- [x] T033 [US1] Update AnalyticsViewModel with new analysis state management in app/src/main/java/com/tiarkaerell/ibstracker/ui/viewmodel/AnalyticsViewModel.kt
- [x] T034 [US1] Connect AnalyticsViewModel to AnalysisRepository for data loading
- [x] T035 [US1] Implement expandable card functionality in SymptomAnalysisCard with Material Design 3 animations
- [x] T036 [US1] Add color-coded probability visualization to TriggerProbabilityBar (red/orange/green)
- [x] T036a [US1] Validate color-coded probability visualization performance meets 60fps requirement per plan.md constraints

**Checkpoint**: At this point, User Story 1 should display basic symptom-trigger correlations with probability percentages

---

## Phase 4: User Story 2 - Understand Correlation Strength (Priority: P2)

**Goal**: Show clear correlation strength indicators and explanations for trigger probabilities

**Independent Test**: Can be tested by viewing correlation indicators (percentages, frequency counts) that clearly explain the relationship between foods and symptoms.

### Enhanced Probability Display for User Story 2

- [x] T037 [P] [US2] Create TriggerDetailsDialog composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/analysis/TriggerDetailsDialog.kt
- [x] T038 [P] [US2] Create CorrelationMetrics composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/analysis/CorrelationMetrics.kt
- [x] T039 [P] [US2] Create InsightText composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/analysis/InsightText.kt
- [x] T040 [US2] Add correlation strength ranking logic to TriggerAnalyzer.kt
- [x] T041 [US2] Implement detailed correlation explanation generation in AnalysisRepositoryImpl.kt
- [x] T042 [US2] Add confidence level indicators to TriggerProbabilityBar.kt
- [x] T043 [US2] Add tap handlers for correlation explanations in SymptomCard.kt
- [x] T044 [US2] Implement statistical summary formatting in ProbabilityEngine.kt
- [x] T045 [US2] Add tooltip/help text system to AnalysisScreen.kt

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently with clear correlation explanations

---

## Phase 5: User Story 3 - Filter and Focus Analysis (Priority: P2)

**Goal**: Provide filtering capabilities for time period, symptom type, and severity analysis

**Independent Test**: Can be tested by applying various filters and verifying that the analysis updates to show only relevant correlations.

### Filtering System for User Story 3

- [x] T046 [P] [US3] Create FilterChips composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/analysis/FilterChips.kt
- [x] T047 [P] [US3] Create DateRangePickerDialog composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/analysis/DateRangePickerDialog.kt
- [x] T048 [P] [US3] Create SeverityFilterSlider composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/analysis/SeverityFilterSlider.kt
- [x] T049 [US3] Implement date range filtering logic in AnalysisRepositoryImpl.kt
- [x] T050 [US3] Implement severity threshold filtering in TriggerAnalyzer.kt
- [x] T051 [US3] Implement symptom type filtering in AnalysisRepositoryImpl.kt
- [x] T052 [US3] Add filter state management to AnalysisViewModel.kt
- [x] T053 [US3] Connect filter UI components to AnalysisViewModel.kt
- [x] T054 [US3] Implement filter persistence in SharedPreferences or Room
- [x] T055 [US3] Add filter clear functionality to FilterChips.kt
- [x] T056 [US3] Add real-time analysis updates when filters change

**Checkpoint**: All filtering options should work independently and update analysis results immediately

---

## Phase 6: User Story 4 - View Clear Explanatory Text (Priority: P3)

**Goal**: Generate plain-language insights and explanations for correlation patterns

**Independent Test**: Can be tested by verifying that each analysis section includes clear, actionable text explanations.

### Natural Language Insights for User Story 4

- [x] T057 [P] [US4] Create InsightEngine class in app/src/main/java/com/tiarkaerell/ibstracker/data/analysis/InsightEngine.kt
- [x] T058 [P] [US4] Create PatternDetectionEngine class in app/src/main/java/com/tiarkaerell/ibstracker/data/analysis/PatternDetectionEngine.kt
- [x] T059 [P] [US4] Create SymptomPattern data class in app/src/main/java/com/tiarkaerell/ibstracker/data/model/SymptomPattern.kt
- [x] T060 [US4] Implement plain-language summary generation in InsightEngine.kt
- [x] T061 [US4] Implement pattern detection algorithms in PatternDetectionEngine.kt
- [x] T062 [US4] Add recommendation generation based on trigger probabilities in InsightEngine.kt
- [x] T063 [US4] Create InsightsTab composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/analysis/InsightsTab.kt
- [x] T064 [US4] Create PatternsTab composable in app/src/main/java/com/tiarkaerell/ibstracker/ui/components/analysis/PatternsTab.kt
- [x] T065 [US4] Add tab navigation to AnalysisScreen.kt (Symptoms, Patterns, Insights tabs)
- [x] T066 [US4] Implement empty state messages for no correlations in AnalysisScreen.kt
- [x] T067 [US4] Add onboarding tooltips for new users in AnalysisScreen.kt

**Checkpoint**: All user stories should now provide clear, accessible insights for all users

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Performance optimization, testing, and final improvements

### Testing & Validation

- [x] T068 [P] Create CorrelationCalculatorTest in app/src/test/java/com/tiarkaerell/ibstracker/analysis/CorrelationCalculatorTest.kt
- [x] T069 [P] Create ProbabilityEngineTest in app/src/test/java/com/tiarkaerell/ibstracker/analysis/ProbabilityEngineTest.kt
- [x] T070 [P] Create TriggerAnalyzerTest in app/src/test/java/com/tiarkaerell/ibstracker/analysis/TriggerAnalyzerTest.kt
- [x] T071 [P] Create AnalysisScreenTest in app/src/androidTest/java/com/tiarkaerell/ibstracker/ui/AnalysisScreenTest.kt
- [x] T072 [P] Create FoodGroupMapperTest in app/src/test/java/com/tiarkaerell/ibstracker/utils/FoodGroupMapperTest.kt

### Performance & Optimization

- [x] T073 [P] Implement correlation result caching in AnalysisRepositoryImpl.kt
- [x] T074 [P] Add background computation with coroutines in AnalyticsViewModel.kt
- [x] T075 [P] Implement lazy loading for large datasets in AnalysisScreen.kt
- [x] T076 [P] Add memory optimization for correlation calculations in TriggerAnalyzer.kt
- [x] T077 [P] Implement incremental cache updates when new data added

### Accessibility & Polish

- [x] T078 [P] Add accessibility labels and descriptions to all UI components
- [x] T079 [P] Implement and validate Material Design 3 theming consistency across analysis components per FR-022
- [x] T080 [P] Add haptic feedback for filter interactions
- [x] T081 [P] Optimize animations for mid-range Android devices
- [x] T082 [P] Add loading states and error handling throughout analysis flow

### Integration & Navigation

- [x] T083 Update existing navigation graph to include improved AnalysisScreen
- [x] T084 Update MainActivity or nav controller to route to new analysis page
- [x] T085 Update existing ViewModelFactory to include AnalysisRepository
- [x] T086 Integrate with existing AppContainer dependency injection
- [x] T086a Validate seamless integration with existing navigation patterns per FR-031

### Documentation & Validation

- [x] T087 [P] Update CLAUDE.md with new analysis feature capabilities
- [x] T088 [P] Create analysis feature documentation for future development  
- [x] T089 Run quickstart.md validation scenarios
- [x] T090 Verify all success criteria from spec.md are met

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-6)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (US1 → US2 → US3 → US4)
- **Polish (Phase 7)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Builds on US1 components but independently testable
- **User Story 3 (P2)**: Can start after Foundational (Phase 2) - Uses US1 components but independently testable  
- **User Story 4 (P3)**: Can start after Foundational (Phase 2) - Uses US1 analysis results but independently testable

### Within Each User Story

- Analysis engine components before UI components
- Data models before business logic
- Core functionality before advanced features
- Basic UI before enhanced interactions
- Story core complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, different user stories can start in parallel
- Within each user story, tasks marked [P] can run in parallel
- Polish tasks marked [P] can all run in parallel after user stories complete

---

## Parallel Example: User Story 1

```bash
# Launch core analysis components together:
Task: "Create TriggerAnalyzer main orchestrator"
Task: "Implement temporal proximity calculation in CorrelationCalculator.kt"
Task: "Implement baseline trigger probability lookup in ProbabilityEngine.kt"

# Launch UI components together:
Task: "Create SymptomCard composable"
Task: "Create TriggerProbabilityBar composable"
Task: "Create SeverityIndicator composable"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently
5. Deploy/demo basic symptom-trigger analysis

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo (Enhanced insights)
4. Add User Story 3 → Test independently → Deploy/Demo (Full filtering)
5. Add User Story 4 → Test independently → Deploy/Demo (Natural language)
6. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (Core analysis)
   - Developer B: User Story 2 (Correlation explanations)
   - Developer C: User Story 3 (Filtering system)
   - Developer D: User Story 4 (Natural language insights)
3. Stories complete and integrate independently

---

## Task Summary

- **Total Tasks**: 90
- **Setup Tasks**: 6
- **Foundational Tasks**: 14
- **User Story 1 (P1)**: 16 tasks
- **User Story 2 (P2)**: 9 tasks  
- **User Story 3 (P2)**: 11 tasks
- **User Story 4 (P3)**: 11 tasks
- **Polish & Testing**: 23 tasks

**Parallel Opportunities**: 45 tasks marked [P] can run in parallel within their phases

**MVP Scope**: Complete Phases 1, 2, and 3 (User Story 1) for basic symptom-trigger analysis functionality

---

## Notes

- [P] tasks = different files, no dependencies within phase
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Android-specific structure follows existing app architecture
- Material Design 3 components used throughout
- Accessibility requirements (48dp touch targets) built into all UI tasks
- Performance optimization tasks ensure <2 second load times for 90 days of data