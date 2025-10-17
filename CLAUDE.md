# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

IBS Tracker is an Android application built with Jetpack Compose for tracking food intake and IBS symptoms. The app uses Room for local database persistence and follows a clean architecture pattern with clear separation between data, domain, and UI layers.

**Package name**: `com.tiarkaerell.ibstracker`
**Min SDK**: 24 (Android 7.0)
**Target SDK**: 34 (Android 14)
**Build System**: Gradle with Kotlin DSL

## Architecture

### Dependency Injection via AppContainer

The app uses **manual dependency injection** through `AppContainer.kt` rather than a DI framework. The `IBSTrackerApplication` class initializes the `AppContainer` on startup, which provides:
- `AppDatabase` (Room database instance)
- `DataRepository` (repository layer)

To access dependencies in UI code, retrieve the container from the application context:
```kotlin
val application = LocalContext.current.applicationContext as IBSTrackerApplication
val repository = application.container.dataRepository
```

### Data Layer Structure

**Room Database** (`data/database/`):
- `AppDatabase.kt`: Room database with `FoodItem` and `Symptom` entities
- `Converters.kt`: TypeConverter for `Date` ↔ `Long` conversion
- `dao/`: Data Access Objects for database operations

**Models** (`data/model/`):
- `FoodItem.kt`: Food entry with name, quantity, date
- `Symptom.kt`: Symptom entry with name, intensity (Int), date

**Repository** (`data/repository/`):
- `DataRepository.kt`: Centralized data access layer that wraps both DAOs
- Returns `Flow<List<T>>` for reactive data streams
- Exposes suspend functions for insert operations

### UI Layer Structure

**Screens** (`ui/screens/`):
- Composable functions for each screen (Dashboard, Food, Symptoms)
- Screens are stateless and receive data/callbacks via parameters

**ViewModels** (`ui/viewmodel/`):
- Each feature has a dedicated ViewModel (e.g., `FoodViewModel`, `SymptomsViewModel`)
- ViewModels use `viewModelScope` for coroutine management
- `ViewModelFactory.kt`: Custom factory for ViewModels that require repository injection

**Theme** (`ui/theme/`):
- Material3 theme configuration in `Theme.kt`, `Color.kt`, `Typography.kt`

### Navigation

The app uses **Navigation Compose** (`androidx.navigation.compose`) for screen navigation. The MainActivity (not visible in current namespace due to refactoring) should contain the `NavHost` that orchestrates navigation between screens.

## Common Development Commands

### Building the App
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build all variants
./gradlew build
```

### Running Tests
```bash
# Run all unit tests
./gradlew test

# Run debug unit tests only
./gradlew testDebugUnitTest

# Run instrumented tests
./gradlew connectedAndroidTest
```

### Code Quality
```bash
# Run lint checks
./gradlew lint

# Run lint and auto-fix issues
./gradlew lintFix

# Run all checks (lint + test)
./gradlew check
```

### Cleaning
```bash
# Clean build directory
./gradlew clean

# Clean and rebuild
./gradlew clean build
```

## Key Dependencies

**Core Android**:
- Kotlin 1.8.20
- AndroidX Core KTX 1.10.1
- AppCompat 1.6.1

**Jetpack Compose**:
- Compose BOM 2023.08.00
- Material3
- Navigation Compose 2.7.7
- Material Icons Extended 1.6.3

**Database**:
- Room 2.6.1 (runtime, compiler, ktx)
- KSP 1.8.20-1.0.11 for annotation processing

## Development Patterns

### Adding New Features

1. **Create data model** in `data/model/` with Room annotations
2. **Create DAO** in `data/database/dao/` with queries
3. **Update AppDatabase** to include new entity and DAO
4. **Update DataRepository** to expose new DAO operations
5. **Create ViewModel** in `ui/viewmodel/` and update `ViewModelFactory`
6. **Create Screen** composable in `ui/screens/`
7. **Add navigation route** in the NavHost

### Database Schema Changes

When modifying Room entities:
1. Update the entity class
2. Increment database version in `AppDatabase`
3. Provide migration strategy or use `fallbackToDestructiveMigration()` for development

### ViewModel Creation

ViewModels require repository injection via `ViewModelFactory`:
```kotlin
val viewModel: FoodViewModel = viewModel(
    factory = ViewModelFactory(repository)
)
```

## Testing

**Unit Tests**: Located in `app/src/test/java/`
- Use JUnit 4.13.2
- Test ViewModels, repositories, and business logic

**Instrumented Tests**: Located in `app/src/androidTest/java/`
- Use AndroidX Test (JUnit 1.1.5, Espresso 3.5.1)
- Test UI components and database operations

## Important Notes

- The app namespace changed from `com.example.ibstracker` to `com.tiarkaerell.ibstracker` during development
- MainActivity is referenced in `AndroidManifest.xml` but the implementation file may need to be created
- Database name: `"ibs-tracker-database"`
- All database operations should be performed on background threads (use suspend functions or Flow)
- Date handling uses `java.util.Date` with Room TypeConverters
