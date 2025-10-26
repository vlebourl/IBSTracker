# Implementation Plan: Improved Analysis Insights

**Branch**: `001-improve-analysis-insights` | **Date**: 2025-10-26 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-improve-analysis-insights/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Redesign the Analysis page to provide a symptom-centric view with probability-based trigger correlations. The feature will transform confusing metrics into clear, actionable insights showing which foods likely triggered each symptom, with Material Design 3 UI components displaying probability percentages and color-coded severity indicators.

## Technical Context

<!--
  ACTION REQUIRED: Replace the content in this section with the technical details
  for the project. The structure here is presented in advisory capacity to guide
  the iteration process.
-->

**Language/Version**: Kotlin 1.8.20 / Android SDK 34  
**Primary Dependencies**: Jetpack Compose, Material3, Room Database, Kotlin Coroutines  
**Storage**: Room SQLite database (existing AppDatabase with FoodItem and Symptom entities)  
**Testing**: JUnit 4.13.2, AndroidX Test 1.1.5, Espresso 3.5.1  
**Target Platform**: Android 7.0+ (minSdk 24), Material Design 3
**Project Type**: mobile - Android application with Jetpack Compose UI  
**Performance Goals**: Analysis page loads in <2 seconds for 90 days of data  
**Constraints**: All calculations local/offline, maintain 60fps smooth scrolling, memory-efficient for large datasets  
**Scale/Scope**: Single-user app, local storage only, ~5-10 screens total

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Note**: No formal constitution defined yet (template file only). Proceeding with Android development best practices:
- Clean Architecture separation (data, domain, UI layers)
- MVVM pattern with ViewModels
- Repository pattern for data access
- Composable UI with Material Design 3
- Testable components with dependency injection

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)
<!--
  ACTION REQUIRED: Replace the placeholder tree below with the concrete layout
  for this feature. Delete unused options and expand the chosen structure with
  real paths (e.g., apps/admin, packages/something). The delivered plan must
  not include Option labels.
-->

```text
app/src/main/java/com/tiarkaerell/ibstracker/
├── data/
│   ├── analysis/          # New analysis package
│   │   ├── CorrelationCalculator.kt
│   │   ├── ProbabilityEngine.kt
│   │   └── TriggerAnalyzer.kt
│   ├── model/
│   │   ├── AnalysisResult.kt    # New model for analysis results
│   │   ├── TriggerProbability.kt # New model for trigger probabilities
│   │   └── CorrelationEvidence.kt # New model for correlation evidence
│   └── repository/
│       └── AnalysisRepository.kt # New repository for analysis data
├── ui/
│   ├── screens/
│   │   └── AnalysisScreen.kt     # Updated analysis screen
│   ├── components/analysis/       # New analysis components
│   │   ├── SymptomCard.kt
│   │   ├── TriggerProbabilityBar.kt
│   │   ├── FilterChips.kt
│   │   └── InsightText.kt
│   └── viewmodel/
│       └── AnalysisViewModel.kt   # Updated view model
└── utils/
    └── FoodGroupMapper.kt        # New utility for grouping similar foods

app/src/test/java/com/tiarkaerell/ibstracker/
└── analysis/
    ├── CorrelationCalculatorTest.kt
    ├── ProbabilityEngineTest.kt
    └── TriggerAnalyzerTest.kt

app/src/androidTest/java/com/tiarkaerell/ibstracker/
└── ui/
    └── AnalysisScreenTest.kt
```

**Structure Decision**: Android mobile app structure following Clean Architecture with separate packages for data (analysis engine, models, repository), UI (screens and components), and utilities. New code will be added to existing package structure under com.tiarkaerell.ibstracker.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
