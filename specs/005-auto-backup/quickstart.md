# Quickstart: Automatic Backup System

**Feature**: 005-auto-backup
**For**: Developers implementing this feature
**Est. Time**: 5-7 hours total (2-3 hours local, 3-4 hours cloud)

## Prerequisites

- ✅ Kotlin 1.8.20 / Android SDK 34
- ✅ Room Database 2.6.1 (already in project)
- ✅ Jetpack Compose (already in project)
- ✅ Git branch `005-auto-backup` checked out

## 30-Second Overview

**What we're building**:
- Local backup after every data change (< 200ms)
- Daily Google Drive sync at 2:00 AM
- Settings UI for backup/restore management
- Comprehensive testing for data integrity

**Key files to create**:
- `data/backup/BackupManager.kt` - Core backup logic
- `data/backup/GoogleDriveBackupWorker.kt` - Cloud sync worker
- `ui/screens/BackupSettingsScreen.kt` - UI for backup management

---

## Step 1: Add Dependencies (5 minutes)

Edit `app/build.gradle.kts`:

```kotlin
dependencies {
    // ... existing dependencies ...

    // WorkManager for scheduled backups
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Google Play Services for authentication
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Google API Client for Android
    implementation("com.google.api-client:google-api-client-android:2.2.0")
    implementation("com.google.http-client:google-http-client-gson:1.43.3")

    // Google Drive API v3
    implementation("com.google.apis:google-api-services-drive:v3-rev20230822-2.0.0")

    // Room testing (already in project, verify version)
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}
```

Sync project with Gradle.

---

## Step 2: Create Data Models (15 minutes)

### 2.1 Create `data/model/backup/` package

```kotlin
// data/model/backup/BackupFile.kt
data class BackupFile(
    val id: String,
    val fileName: String,
    val filePath: String,
    val location: BackupLocation,
    val timestamp: Long,
    val sizeBytes: Long,
    val databaseVersion: Int,
    val checksum: String,
    val status: BackupStatus,
    val createdAt: Long = System.currentTimeMillis()
)

enum class BackupLocation { LOCAL, CLOUD }

enum class BackupStatus { AVAILABLE, UPLOADING, DOWNLOADING, FAILED, CORRUPTED }
```

```kotlin
// data/model/backup/BackupSettings.kt
data class BackupSettings(
    val localBackupsEnabled: Boolean = true,
    val cloudSyncEnabled: Boolean = true,
    val lastLocalBackupTimestamp: Long? = null,
    val lastCloudSyncTimestamp: Long? = null,
    val googleAccountEmail: String? = null,
    val isGoogleSignedIn: Boolean = false
)
```

```kotlin
// data/model/backup/BackupResult.kt
sealed class BackupResult {
    data class Success(
        val backupFile: BackupFile,
        val durationMs: Long
    ) : BackupResult()

    data class Failure(
        val error: BackupError,
        val message: String,
        val cause: Throwable? = null
    ) : BackupResult()
}

enum class BackupError {
    STORAGE_FULL,
    DATABASE_LOCKED,
    CHECKPOINT_FAILED,
    COPY_FAILED,
    CHECKSUM_MISMATCH,
    UPLOAD_FAILED,
    AUTHENTICATION_FAILED,
    NETWORK_UNAVAILABLE,
    UNKNOWN
}
```

**See**: [data-model.md](data-model.md) for complete data model definitions.

---

## Step 3: Implement BackupManager (45-60 minutes)

### 3.1 Create `data/backup/BackupManager.kt`

```kotlin
class BackupManager(
    private val context: Context,
    private val database: AppDatabase
) {
    private val backupDir = context.filesDir

    suspend fun createLocalBackup(): BackupResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        try {
            // Step 1: Check storage space
            if (!hasEnoughStorageSpace(2 * 1024 * 1024)) { // 2MB buffer
                return@withContext BackupResult.Failure(
                    BackupError.STORAGE_FULL,
                    "Insufficient storage space"
                )
            }

            // Step 2: Execute WAL checkpoint
            database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)")

            // Step 3: Generate backup filename
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val version = AppDatabase.DATABASE_VERSION
            val filename = "ibstracker_v${version}_${timestamp}.db"
            val backupFile = File(backupDir, filename)

            // Step 4: Copy database with checksum
            val dbFile = context.getDatabasePath("ibs-tracker-database")
            val checksum = dbFile.copyToWithChecksum(backupFile)

            // Step 5: Store checksum
            File(backupDir, "${filename}.sha256").writeText(checksum)

            // Step 6: Cleanup old backups
            cleanupOldBackups(7)

            val duration = System.currentTimeMillis() - startTime

            BackupResult.Success(
                backupFile = BackupFile(
                    id = UUID.randomUUID().toString(),
                    fileName = filename,
                    filePath = backupFile.absolutePath,
                    location = BackupLocation.LOCAL,
                    timestamp = System.currentTimeMillis(),
                    sizeBytes = backupFile.length(),
                    databaseVersion = version,
                    checksum = checksum,
                    status = BackupStatus.AVAILABLE
                ),
                durationMs = duration
            )
        } catch (e: Exception) {
            BackupResult.Failure(
                BackupError.COPY_FAILED,
                "Backup failed: ${e.message}",
                e
            )
        }
    }

    private fun File.copyToWithChecksum(target: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        this.inputStream().buffered(8192).use { input ->
            target.outputStream().buffered(8192).use { output ->
                val buffer = ByteArray(8192)
                var bytesRead = input.read(buffer)
                while (bytesRead >= 0) {
                    output.write(buffer, 0, bytesRead)
                    digest.update(buffer, 0, bytesRead)
                    bytesRead = input.read(buffer)
                }
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun cleanupOldBackups(retentionCount: Int) {
        backupDir.listFiles { file ->
            file.name.startsWith("ibstracker_v") && file.extension == "db"
        }?.sortedByDescending { it.lastModified() }
            ?.drop(retentionCount)
            ?.forEach { file ->
                file.delete()
                File("${file.path}.sha256").delete()
            }
    }

    private fun hasEnoughStorageSpace(requiredBytes: Long): Boolean {
        val statFs = StatFs(backupDir.path)
        val availableBytes = statFs.availableBlocksLong * statFs.blockSizeLong
        return availableBytes > requiredBytes * 2
    }
}
```

**See**: [contracts/BackupManager.kt](contracts/BackupManager.kt) for complete API contract.

---

## Step 4: Add Backup Triggers to DataRepository (15 minutes)

Edit `data/repository/DataRepository.kt`:

```kotlin
class DataRepository(
    private val foodItemDao: FoodItemDao,
    private val symptomDao: SymptomDao,
    private val commonFoodDao: CommonFoodDao,
    private val foodUsageStatsDao: FoodUsageStatsDao,
    private val backupManager: BackupManager  // ADD THIS
) {
    // ... existing code ...

    suspend fun insertFoodItem(foodItem: FoodItem) {
        foodItemDao.insertFoodItem(foodItem)
        // Trigger backup after insert
        backupManager.createLocalBackup()
    }

    suspend fun updateFoodItem(foodItem: FoodItem) {
        foodItemDao.updateFoodItem(foodItem)
        // Trigger backup after update
        backupManager.createLocalBackup()
    }

    suspend fun deleteFoodItem(foodItem: FoodItem) {
        foodItemDao.deleteFoodItem(foodItem)
        // Trigger backup after delete
        backupManager.createLocalBackup()
    }

    // Repeat for symptom operations...
    suspend fun insertSymptom(symptom: Symptom) {
        symptomDao.insertSymptom(symptom)
        backupManager.createLocalBackup()
    }
}
```

---

## Step 5: Update AppContainer (10 minutes)

Edit `AppContainer.kt`:

```kotlin
class AppContainer(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)

    // ADD: BackupManager
    val backupManager: BackupManager by lazy {
        BackupManager(context, database)
    }

    // MODIFY: DataRepository to include backupManager
    val dataRepository: DataRepository by lazy {
        DataRepository(
            database.foodItemDao(),
            database.symptomDao(),
            database.commonFoodDao(),
            database.foodUsageStatsDao(),
            backupManager  // Add this parameter
        )
    }
}
```

---

## Step 6: Write Tests (30-45 minutes)

### 6.1 Create `androidTest/backup/BackupManagerTest.kt`

```kotlin
@RunWith(AndroidJUnit4::class)
class BackupManagerTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var backupManager: BackupManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        backupManager = BackupManager(context, database)
    }

    @Test
    fun testCreateLocalBackup_success() = runTest {
        // Create test data
        database.foodItemDao().insertFoodItem(
            FoodItem(name = "Test Food", quantity = "100g", timestamp = Date())
        )

        // Create backup
        val result = backupManager.createLocalBackup()

        // Verify success
        assertThat(result, instanceOf(BackupResult.Success::class.java))
        val success = result as BackupResult.Success
        assertThat(success.backupFile.fileName, startsWith("ibstracker_v"))
        assertThat(success.durationMs, lessThan(200L)) // < 200ms requirement
    }

    @Test
    fun testBackupChecksum_integrity() = runTest {
        // Create backup
        val result = backupManager.createLocalBackup() as BackupResult.Success
        val backupFile = File(result.backupFile.filePath)

        // Verify checksum file exists
        val checksumFile = File("${backupFile.path}.sha256")
        assertThat(checksumFile.exists(), `is`(true))

        // Verify checksum length (SHA-256 = 64 hex chars)
        val checksum = checksumFile.readText()
        assertThat(checksum.length, `is`(64))
    }

    @Test
    fun testCleanupOldBackups_keepsSevenMostRecent() = runTest {
        // Create 10 backups
        repeat(10) {
            backupManager.createLocalBackup()
            delay(100) // Ensure different timestamps
        }

        // Verify only 7 backups remain
        val backups = context.filesDir.listFiles { file ->
            file.name.startsWith("ibstracker_v") && file.extension == "db"
        }
        assertThat(backups?.size, `is`(7))
    }

    @After
    fun teardown() {
        database.close()
        // Cleanup test backups
        context.filesDir.listFiles { file ->
            file.name.startsWith("ibstracker_v")
        }?.forEach { it.delete() }
    }
}
```

### 6.2 Run tests

```bash
./gradlew test
./gradlew connectedAndroidTest
```

**All tests must pass before proceeding to cloud sync implementation.**

---

## Step 7: Implement Google Drive Sync (90-120 minutes)

### 7.1 Create `data/backup/GoogleDriveBackupWorker.kt`

```kotlin
class GoogleDriveBackupWorkerImpl(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Step 1: Check if sync enabled
            val settings = getBackupSettings()
            if (!settings.cloudSyncEnabled) {
                return@withContext Result.success()
            }

            // Step 2: Check Google Sign-In
            if (!settings.isGoogleSignedIn) {
                return@withContext Result.failure()
            }

            // Step 3: Create local backup
            val backupManager = BackupManager(applicationContext, AppDatabase.getDatabase(applicationContext))
            val backupResult = backupManager.createLocalBackup()

            if (backupResult !is BackupResult.Success) {
                return@withContext if (runAttemptCount < 3) Result.retry() else Result.failure()
            }

            // Step 4: Upload to Google Drive
            val driveService = getDriveService()
            uploadToGoogleDrive(backupResult.backupFile, driveService)

            // Step 5: Cleanup old cloud backups
            cleanupOldCloudBackups(driveService, 30)

            Result.success()
        } catch (e: IOException) {
            // Network error - retry
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        } catch (e: SecurityException) {
            // Auth error - don't retry
            Result.failure()
        }
    }

    private fun getDriveService(): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            applicationContext,
            listOf(DriveScopes.DRIVE_APPDATA)
        )

        return Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("IBS Tracker").build()
    }

    private suspend fun uploadToGoogleDrive(backupFile: BackupFile, driveService: Drive) {
        val fileMetadata = com.google.api.services.drive.model.File().apply {
            name = backupFile.fileName
            parents = listOf("appDataFolder")
        }

        val mediaContent = FileContent("application/x-sqlite3", File(backupFile.filePath))

        driveService.files().create(fileMetadata, mediaContent)
            .setFields("id, name")
            .execute()
    }
}
```

### 7.2 Schedule the worker

Edit `IBSTrackerApplication.kt`:

```kotlin
class IBSTrackerApplication : Application() {
    val container by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()

        // Schedule cloud backup if enabled
        scheduleCloudBackup()
    }

    private fun scheduleCloudBackup() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .setRequiresCharging(true)
            .setRequiresBatteryNotLow(true)
            .build()

        val backupRequest = PeriodicWorkRequestBuilder<GoogleDriveBackupWorkerImpl>(
            24, TimeUnit.HOURS,
            1, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "google_drive_backup",
            ExistingPeriodicWorkPolicy.KEEP,
            backupRequest
        )
    }
}
```

**See**: [contracts/GoogleDriveBackupWorker.kt](contracts/GoogleDriveBackupWorker.kt) for complete API contract.

---

## Step 8: Create Settings UI (60-90 minutes)

### 8.1 Create `ui/screens/BackupSettingsScreen.kt`

```kotlin
@Composable
fun BackupSettingsScreen(
    viewModel: BackupViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    val localBackups by viewModel.localBackups.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Local Backups Section
            Text("Local Backups", style = MaterialTheme.typography.titleMedium)

            Switch(
                checked = settings.localBackupsEnabled,
                onCheckedChange = { viewModel.setLocalBackupsEnabled(it) }
            )

            Text("Last backup: ${settings.lastLocalBackupTimestamp?.formatRelativeTime() ?: "Never"}")
            Text("${localBackups.size} backups available")

            Button(onClick = { viewModel.createLocalBackup() }) {
                Text("Backup Now")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cloud Sync Section
            Text("Google Drive Sync", style = MaterialTheme.typography.titleMedium)

            Switch(
                checked = settings.cloudSyncEnabled,
                onCheckedChange = { viewModel.setCloudSyncEnabled(it) }
            )

            Text(syncStatus.toDisplayString())

            // Restore Section
            Text("Restore", style = MaterialTheme.typography.titleMedium)

            LazyColumn {
                items(localBackups) { backup ->
                    BackupListItem(
                        backup = backup,
                        onRestore = { viewModel.restoreBackup(backup) }
                    )
                }
            }
        }
    }
}
```

### 8.2 Create `ui/viewmodel/BackupViewModel.kt`

```kotlin
class BackupViewModel(
    private val backupRepository: BackupRepository
) : ViewModel() {

    val settings = backupRepository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.Lazily, BackupSettings())

    val localBackups = backupRepository.observeLocalBackups()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val syncStatus = backupRepository.observeSyncStatus()
        .stateIn(viewModelScope, SharingStarted.Lazily, SyncStatus(SyncState.NEVER))

    fun setLocalBackupsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            backupRepository.setLocalBackupsEnabled(enabled)
        }
    }

    fun setCloudSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            backupRepository.setCloudSyncEnabled(enabled)
        }
    }

    fun createLocalBackup() {
        viewModelScope.launch {
            backupRepository.createLocalBackup()
        }
    }

    fun restoreBackup(backup: BackupFile) {
        viewModelScope.launch {
            val result = backupRepository.restoreFromBackup(backup)
            // TODO: Show success/error message
        }
    }
}
```

---

## Step 9: Manual Testing (30 minutes)

### 9.1 Test local backups

1. Run app on emulator or device
2. Add a food item
3. Check backup created:
   ```bash
   adb shell "run-as com.tiarkaerell.ibstracker ls -la files/ | grep ibstracker_v"
   ```
4. Add 10 more food items
5. Verify only 7 backups remain

### 9.2 Test restore

1. Note current data (count food items)
2. Add new food item
3. Open Settings → Backup & Restore
4. Select a backup from 5 minutes ago
5. Tap "Restore"
6. Verify app shows restored data (newest food item is gone)

### 9.3 Test cloud sync

1. Sign in to Google account in app
2. Enable "Google Drive sync"
3. Tap "Backup now"
4. Check WorkManager logs:
   ```bash
   adb logcat -s WM-WorkerWrapper
   ```
5. Verify backup appears in Google Drive appDataFolder (requires API query or test script)

---

## Step 10: Integration Testing (45 minutes)

### 10.1 Create end-to-end test

```kotlin
@RunWith(AndroidJUnit4::class)
class BackupIntegrationTest {

    @Test
    fun testFullBackupRestoreFlow() = runTest {
        // 1. Create initial data
        val foodItem = FoodItem(name = "Test Food", quantity = "100g", timestamp = Date())
        repository.insertFoodItem(foodItem)

        // 2. Create backup
        val backupResult = backupManager.createLocalBackup()
        assertThat(backupResult, instanceOf(BackupResult.Success::class.java))
        val backup = (backupResult as BackupResult.Success).backupFile

        // 3. Modify data
        repository.deleteFoodItem(foodItem)
        val remainingItems = repository.getFoodItems().first()
        assertThat(remainingItems.isEmpty(), `is`(true))

        // 4. Restore from backup
        val restoreResult = restoreManager.restoreFromBackup(backup)
        assertThat(restoreResult, instanceOf(RestoreResult.Success::class.java))

        // 5. Verify data restored
        val restoredItems = repository.getFoodItems().first()
        assertThat(restoredItems.size, `is`(1))
        assertThat(restoredItems[0].name, `is`("Test Food"))
    }
}
```

Run integration tests:
```bash
./gradlew connectedAndroidTest
```

---

## Common Issues & Solutions

### Issue: "Backup takes > 200ms"
**Solution**: Ensure you're using 8192-byte buffers and executing WAL checkpoint before copy.

### Issue: "Restored database is empty"
**Solution**: Check if WAL checkpoint was executed. Without checkpoint, backup only contains partial data.

### Issue: "WorkManager doesn't run at 2:00 AM"
**Solution**: PeriodicWorkRequest cannot schedule exact times. Use 24h interval + 1h flex period for 1:00-2:00 AM window.

### Issue: "Google Drive upload fails"
**Solution**: Verify Google Sign-In is active and DRIVE_APPDATA scope is granted.

### Issue: "Database version mismatch on restore"
**Solution**: Check `backupFile.databaseVersion` against `AppDatabase.DATABASE_VERSION`. Show error if versions incompatible.

---

## Next Steps

After completing this quickstart:

1. ✅ All tests pass (unit + integration)
2. ✅ Local backups work after data changes
3. ✅ Cloud sync scheduled and working
4. ✅ Settings UI functional
5. → **Run `/speckit.tasks` to break down remaining work into atomic tasks**
6. → Review [research.md](research.md) for advanced patterns
7. → Review [data-model.md](data-model.md) for complete data structures

---

## Resources

- [API Contracts](contracts/) - Interface definitions for all components
- [Research Findings](research.md) - Best practices and implementation patterns
- [Data Model](data-model.md) - Complete data structures and relationships
- [Feature Spec](spec.md) - Original requirements and acceptance criteria

**Estimated Total Time**: 5-7 hours (2-3 hours local, 3-4 hours cloud)

**Success Criteria**:
- Local backup < 200ms ✓
- Restore < 3 seconds ✓
- Cloud upload < 10 seconds ✓
- All tests passing ✓
