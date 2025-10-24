package com.tiarkaerell.ibstracker.data.sync

import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.model.Symptom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

// Custom exceptions for backup/restore errors
class PasswordRequiredException(message: String) : Exception(message)
class IncorrectPasswordException(message: String) : Exception(message)

/**
 * Manages optional password-encrypted backups to Google Drive.
 *
 * ENCRYPTION BEHAVIOR:
 * - By default, backups are stored as plaintext JSON (protected only by Google Drive auth)
 * - If user sets a backup password, backups are encrypted with AES-256-GCM
 * - Password is derived to encryption key using PBKDF2 with 100,000 iterations
 * - Encrypted backups use .enc extension, unencrypted use .json
 *
 * PORTABILITY:
 * - Unencrypted backups: Work on any device with Google Drive access
 * - Encrypted backups: Work on any device if you know the password
 * - Password can be changed, but old backups keep their original password
 *
 * SECURITY NOTES:
 * - Without password: Data visible to anyone with Google account access
 * - With password: Data encrypted, but password must be remembered (no recovery)
 */
class GoogleDriveBackup(
    private val context: Context,
    private val database: AppDatabase,
    private val settingsRepository: com.tiarkaerell.ibstracker.data.repository.SettingsRepository
) {

    private val json = Json { prettyPrint = true }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    private val encryptionManager = com.tiarkaerell.ibstracker.data.security.EncryptionManager()
    
    @Serializable
    data class BackupData(
        val version: Int = 2,
        val timestamp: String,
        val foodItems: List<SerializableFoodItem>,
        val symptoms: List<SerializableSymptom>,
        val userProfile: SerializableUserProfile? = null,
        val language: String? = null,
        val units: String? = null
    )
    
    @Serializable
    data class SerializableFoodItem(
        val id: Int,
        val name: String,
        val quantity: String,
        val category: String,
        val timestamp: Long
    )
    
    @Serializable
    data class SerializableSymptom(
        val id: Int,
        val name: String,
        val intensity: Int,
        val timestamp: Long
    )

    @Serializable
    data class SerializableUserProfile(
        val dateOfBirth: Long? = null,
        val sex: String = "NOT_SPECIFIED",
        val heightCm: Int? = null,
        val weightKg: Float? = null,
        val activityLevel: String = "NOT_SPECIFIED",
        val ibsDiagnosisDate: Long? = null,
        val ibsType: String = "NOT_SPECIFIED",
        val hasAllergies: Boolean = false,
        val allergyNotes: String = "",
        val medicationNotes: String = "",
        val lastUpdated: Long = System.currentTimeMillis()
    )

    /**
     * Creates a backup to Google Drive
     * @param accessToken OAuth 2.0 access token from AuthorizationClient
     */
    suspend fun createBackup(accessToken: String?): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (accessToken == null) {
                return@withContext Result.failure(Exception("Not authorized. Please sign in to Google Drive."))
            }

            val driveService = getDriveService(accessToken)
                ?: return@withContext Result.failure(Exception("Failed to initialize Drive service"))
            
            // Get data from database
            val foodItems = database.foodItemDao().getAllFoodItems().first()
            val symptoms = database.symptomDao().getAll().first()

            // Get settings
            val currentLanguage = settingsRepository.languageFlow.first()
            val currentUnits = settingsRepository.unitsFlow.first()
            val currentProfile = settingsRepository.userProfileFlow.first()

            // Convert to serializable format
            val backupData = BackupData(
                timestamp = dateFormat.format(Date()),
                foodItems = foodItems.map {
                    SerializableFoodItem(
                        id = it.id.toInt(),
                        name = it.name,
                        quantity = it.quantity,
                        category = it.category.name,
                        timestamp = it.timestamp.time
                    )
                },
                symptoms = symptoms.map {
                    SerializableSymptom(
                        id = it.id,
                        name = it.name,
                        intensity = it.intensity,
                        timestamp = it.date.time
                    )
                },
                userProfile = SerializableUserProfile(
                    dateOfBirth = currentProfile.dateOfBirth,
                    sex = currentProfile.sex.name,
                    heightCm = currentProfile.heightCm,
                    weightKg = currentProfile.weightKg,
                    activityLevel = currentProfile.activityLevel.name,
                    ibsDiagnosisDate = currentProfile.ibsDiagnosisDate,
                    ibsType = currentProfile.ibsType.name,
                    hasAllergies = currentProfile.hasAllergies,
                    allergyNotes = currentProfile.allergyNotes,
                    medicationNotes = currentProfile.medicationNotes,
                    lastUpdated = currentProfile.lastUpdated
                ),
                language = currentLanguage.code,
                units = currentUnits.name
            )
            
            // Convert to JSON
            val jsonContent = json.encodeToString(backupData)

            // Check if password encryption is enabled
            val password = settingsRepository.getBackupPassword()
            val (fileContent, fileName) = if (!password.isNullOrEmpty()) {
                // Encrypt with password
                val encryptedContent = encryptionManager.encrypt(jsonContent, password)
                val content = com.google.api.client.http.ByteArrayContent.fromString("text/plain", encryptedContent)
                val name = "ibs_tracker_backup_${backupData.timestamp}.enc"
                Pair(content, name)
            } else {
                // Store as plaintext JSON
                val content = com.google.api.client.http.ByteArrayContent.fromString("application/json", jsonContent)
                val name = "ibs_tracker_backup_${backupData.timestamp}.json"
                Pair(content, name)
            }

            // Create file metadata
            val fileMetadata = File()
                .setName(fileName)
                .setParents(listOf(getAppFolderId(driveService)))
            val uploadedFile = driveService.files().create(fileMetadata, fileContent)
                .setFields("id")
                .execute()
            
            Result.success("Backup created: ${uploadedFile.id}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Restores a backup from Google Drive (replaces all local data)
     * @param fileId Google Drive file ID
     * @param accessToken OAuth 2.0 access token from AuthorizationClient
     */
    suspend fun restoreFromBackup(fileId: String, accessToken: String?): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (accessToken == null) {
                return@withContext Result.failure(Exception("Not authorized. Please sign in to Google Drive."))
            }

            val driveService = getDriveService(accessToken)
                ?: return@withContext Result.failure(Exception("Failed to initialize Drive service"))
            
            // Download file content
            val outputStream = ByteArrayOutputStream()
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            val jsonContent = outputStream.toString("UTF-8")
            
            // Parse backup data
            val backupData = json.decodeFromString<BackupData>(jsonContent)
            
            // Clear existing data
            database.foodItemDao().deleteAll()
            database.symptomDao().deleteAllSymptoms()

            // Restore food items
            backupData.foodItems.forEach { item ->
                val foodItem = FoodItem(
                    id = 0, // Let Room auto-generate new IDs
                    name = item.name,
                    quantity = item.quantity,
                    category = com.tiarkaerell.ibstracker.data.model.FoodCategory.valueOf(item.category),
                    timestamp = Date(item.timestamp)
                )
                database.foodItemDao().insert(foodItem)
            }
            
            // Restore symptoms
            backupData.symptoms.forEach { item ->
                val symptom = Symptom(
                    id = 0, // Let Room auto-generate new IDs
                    name = item.name,
                    intensity = item.intensity,
                    date = Date(item.timestamp)
                )
                database.symptomDao().insert(symptom)
            }
            
            Result.success("Restored ${backupData.foodItems.size} food items and ${backupData.symptoms.size} symptoms")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Lists all backups in Google Drive
     * @param accessToken OAuth 2.0 access token from AuthorizationClient
     */
    suspend fun listBackups(accessToken: String?): Result<List<DriveFile>> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (accessToken == null) {
                return@withContext Result.failure(Exception("Not authorized. Please sign in to Google Drive."))
            }

            val driveService = getDriveService(accessToken)
                ?: return@withContext Result.failure(Exception("Failed to initialize Drive service"))
            
            val folderId = getAppFolderId(driveService)
            val result = driveService.files().list()
                .setQ("'$folderId' in parents and name contains 'ibs_tracker_backup'")
                .setOrderBy("createdTime desc")
                .setFields("files(id,name,createdTime,size)")
                .execute()
            
            val backups = result.files.map { file ->
                DriveFile(
                    id = file.id,
                    name = file.name,
                    createdTime = file.createdTime?.value ?: 0L,
                    size = file.getSize() ?: 0L
                )
            }
            
            Result.success(backups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Creates Drive service using access token from modern authorization flow
     * @param accessToken OAuth 2.0 access token from AuthorizationClient
     */
    private fun getDriveService(accessToken: String): Drive? {
        return try {
            // Create GoogleCredentials with access token
            val credentials = GoogleCredentials.newBuilder()
                .setAccessToken(AccessToken(accessToken, null))
                .build()

            // Wrap credentials in HttpCredentialsAdapter for Drive API
            val requestInitializer: HttpRequestInitializer = HttpCredentialsAdapter(credentials)

            Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                requestInitializer
            )
                .setApplicationName("IBS Tracker")
                .build()
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getAppFolderId(driveService: Drive): String {
        return try {
            // Check if app folder exists
            val result = driveService.files().list()
                .setQ("name='IBS Tracker Backups' and mimeType='application/vnd.google-apps.folder'")
                .setFields("files(id)")
                .execute()
            
            if (result.files.isNotEmpty()) {
                result.files[0].id
            } else {
                // Create app folder
                val folderMetadata = File()
                    .setName("IBS Tracker Backups")
                    .setMimeType("application/vnd.google-apps.folder")
                
                val folder = driveService.files().create(folderMetadata)
                    .setFields("id")
                    .execute()
                
                folder.id
            }
        } catch (e: IOException) {
            "root" // Fallback to root folder
        }
    }
    
    data class DriveFile(
        val id: String,
        val name: String,
        val createdTime: Long,
        val size: Long
    )

    enum class MergeStrategy {
        KEEP_LOCAL,      // Keep local data, ignore backup conflicts
        KEEP_BACKUP,     // Overwrite local with backup data
        KEEP_BOTH        // Keep both (duplicate entries)
    }

    data class BackupMetadata(
        val id: String,
        val name: String,
        val createdTime: Long,
        val timestamp: String,
        val foodItemCount: Int,
        val symptomCount: Int
    )

    /**
     * Gets metadata for a specific backup
     * @param fileId Google Drive file ID
     * @param accessToken OAuth 2.0 access token from AuthorizationClient
     */
    suspend fun getBackupMetadata(fileId: String, accessToken: String?): Result<BackupMetadata> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (accessToken == null) {
                return@withContext Result.failure(Exception("Not authorized. Please sign in to Google Drive."))
            }

            val driveService = getDriveService(accessToken)
                ?: return@withContext Result.failure(Exception("Failed to initialize Drive service"))

            // Get file metadata first to check if encrypted
            val file = driveService.files().get(fileId)
                .setFields("id,name,createdTime")
                .execute()

            // Download and parse file to get counts
            val outputStream = ByteArrayOutputStream()
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            val fileContent = outputStream.toString("UTF-8")

            // Decrypt if file is encrypted (ends with .enc)
            val jsonContent = if (file.name.endsWith(".enc")) {
                val password = settingsRepository.getBackupPassword()
                if (password.isNullOrEmpty()) {
                    return@withContext Result.failure(Exception("Backup is encrypted but no password is configured. Set your backup password in Settings."))
                }
                try {
                    encryptionManager.decrypt(fileContent, password)
                } catch (e: Exception) {
                    return@withContext Result.failure(Exception("Failed to decrypt backup. Password may be incorrect."))
                }
            } else {
                fileContent // Unencrypted backup
            }

            val backupData = json.decodeFromString<BackupData>(jsonContent)

            Result.success(BackupMetadata(
                id = file.id,
                name = file.name,
                createdTime = file.createdTime?.value ?: 0L,
                timestamp = backupData.timestamp,
                foodItemCount = backupData.foodItems.size,
                symptomCount = backupData.symptoms.size
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Restores a backup with merge strategy
     * @param fileId Google Drive file ID
     * @param mergeStrategy How to handle conflicts with local data
     * @param password Optional password for encrypted backups
     * @param accessToken OAuth 2.0 access token from AuthorizationClient
     */
    suspend fun restoreWithMerge(
        fileId: String,
        mergeStrategy: MergeStrategy,
        password: String? = null,
        accessToken: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (accessToken == null) {
                return@withContext Result.failure(Exception("Not authorized. Please sign in to Google Drive."))
            }

            val driveService = getDriveService(accessToken)
                ?: return@withContext Result.failure(Exception("Failed to initialize Drive service"))

            // Get file metadata to check if encrypted
            val fileMetadata = driveService.files().get(fileId)
                .setFields("name")
                .execute()

            // Download file content
            val outputStream = ByteArrayOutputStream()
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            val fileContent = outputStream.toString("UTF-8")

            // Decrypt if file is encrypted (ends with .enc)
            val jsonContent = if (fileMetadata.name.endsWith(".enc")) {
                val decryptPassword = password ?: settingsRepository.getBackupPassword()
                if (decryptPassword.isNullOrEmpty()) {
                    return@withContext Result.failure(PasswordRequiredException("Backup is encrypted. Password required."))
                }
                try {
                    encryptionManager.decrypt(fileContent, decryptPassword)
                } catch (e: Exception) {
                    return@withContext Result.failure(IncorrectPasswordException("Failed to decrypt backup. Password may be incorrect."))
                }
            } else {
                fileContent // Unencrypted backup
            }

            // Parse backup data
            val backupData = json.decodeFromString<BackupData>(jsonContent)

            when (mergeStrategy) {
                MergeStrategy.KEEP_LOCAL -> {
                    // Get existing data
                    val existingFoodItems = database.foodItemDao().getAllFoodItems().first()
                    val existingSymptoms = database.symptomDao().getAll().first()

                    // Only add items that don't conflict (by timestamp)
                    val existingFoodTimestamps = existingFoodItems.map { it.timestamp.time }.toSet()
                    val existingSymptomTimestamps = existingSymptoms.map { it.date.time }.toSet()

                    var addedFood = 0
                    var addedSymptoms = 0

                    backupData.foodItems.forEach { item ->
                        if (item.timestamp !in existingFoodTimestamps) {
                            val foodItem = FoodItem(
                                id = 0,
                                name = item.name,
                                quantity = item.quantity,
                                category = com.tiarkaerell.ibstracker.data.model.FoodCategory.valueOf(item.category),
                                timestamp = Date(item.timestamp)
                            )
                            database.foodItemDao().insert(foodItem)
                            addedFood++
                        }
                    }

                    backupData.symptoms.forEach { item ->
                        if (item.timestamp !in existingSymptomTimestamps) {
                            val symptom = Symptom(
                                id = 0,
                                name = item.name,
                                intensity = item.intensity,
                                date = Date(item.timestamp)
                            )
                            database.symptomDao().insert(symptom)
                            addedSymptoms++
                        }
                    }

                    Result.success("Added $addedFood food items and $addedSymptoms symptoms (kept local conflicts)")
                }

                MergeStrategy.KEEP_BACKUP -> {
                    // Get existing data
                    val existingFoodItems = database.foodItemDao().getAllFoodItems().first()
                    val existingSymptoms = database.symptomDao().getAll().first()

                    // Delete conflicting entries
                    val backupFoodTimestamps = backupData.foodItems.map { it.timestamp }.toSet()
                    val backupSymptomTimestamps = backupData.symptoms.map { it.timestamp }.toSet()

                    existingFoodItems.forEach { item ->
                        if (item.timestamp.time in backupFoodTimestamps) {
                            database.foodItemDao().delete(item)
                        }
                    }

                    existingSymptoms.forEach { item ->
                        if (item.date.time in backupSymptomTimestamps) {
                            database.symptomDao().delete(item)
                        }
                    }

                    // Add all backup items
                    backupData.foodItems.forEach { item ->
                        val foodItem = FoodItem(
                            id = 0,
                            name = item.name,
                            quantity = item.quantity,
                            category = com.tiarkaerell.ibstracker.data.model.FoodCategory.valueOf(item.category),
                            timestamp = Date(item.timestamp)
                        )
                        database.foodItemDao().insert(foodItem)
                    }

                    backupData.symptoms.forEach { item ->
                        val symptom = Symptom(
                            id = 0,
                            name = item.name,
                            intensity = item.intensity,
                            date = Date(item.timestamp)
                        )
                        database.symptomDao().insert(symptom)
                    }

                    Result.success("Restored ${backupData.foodItems.size} food items and ${backupData.symptoms.size} symptoms (overwrote conflicts)")
                }

                MergeStrategy.KEEP_BOTH -> {
                    // Simply add all items (may create duplicates)
                    backupData.foodItems.forEach { item ->
                        val foodItem = FoodItem(
                            id = 0,
                            name = item.name,
                            quantity = item.quantity,
                            category = com.tiarkaerell.ibstracker.data.model.FoodCategory.valueOf(item.category),
                            timestamp = Date(item.timestamp)
                        )
                        database.foodItemDao().insert(foodItem)
                    }

                    backupData.symptoms.forEach { item ->
                        val symptom = Symptom(
                            id = 0,
                            name = item.name,
                            intensity = item.intensity,
                            date = Date(item.timestamp)
                        )
                        database.symptomDao().insert(symptom)
                    }

                    Result.success("Restored ${backupData.foodItems.size} food items and ${backupData.symptoms.size} symptoms (kept all)")
                }
            }

            // Restore settings (for all merge strategies)
            backupData.language?.let { languageCode ->
                val language = com.tiarkaerell.ibstracker.data.model.Language.fromCode(languageCode)
                settingsRepository.setLanguage(language)
            }

            backupData.units?.let { unitsName ->
                val units = com.tiarkaerell.ibstracker.data.model.Units.fromName(unitsName)
                settingsRepository.setUnits(units)
            }

            backupData.userProfile?.let { profile ->
                val userProfile = com.tiarkaerell.ibstracker.data.model.UserProfile(
                    dateOfBirth = profile.dateOfBirth,
                    sex = try {
                        com.tiarkaerell.ibstracker.data.model.Sex.valueOf(profile.sex)
                    } catch (e: Exception) {
                        com.tiarkaerell.ibstracker.data.model.Sex.NOT_SPECIFIED
                    },
                    heightCm = profile.heightCm,
                    weightKg = profile.weightKg,
                    activityLevel = try {
                        com.tiarkaerell.ibstracker.data.model.ActivityLevel.valueOf(profile.activityLevel)
                    } catch (e: Exception) {
                        com.tiarkaerell.ibstracker.data.model.ActivityLevel.NOT_SPECIFIED
                    },
                    ibsDiagnosisDate = profile.ibsDiagnosisDate,
                    ibsType = try {
                        com.tiarkaerell.ibstracker.data.model.IBSType.valueOf(profile.ibsType)
                    } catch (e: Exception) {
                        com.tiarkaerell.ibstracker.data.model.IBSType.NOT_SPECIFIED
                    },
                    hasAllergies = profile.hasAllergies,
                    allergyNotes = profile.allergyNotes,
                    medicationNotes = profile.medicationNotes,
                    lastUpdated = profile.lastUpdated
                )
                settingsRepository.updateUserProfile(userProfile)
            }

            // Return success with appropriate message (the result from the when block)
            when (mergeStrategy) {
                MergeStrategy.KEEP_LOCAL -> Result.success("Restored backup with local data priority")
                MergeStrategy.KEEP_BACKUP -> Result.success("Restored backup with backup data priority")
                MergeStrategy.KEEP_BOTH -> Result.success("Restored all backup and local data")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}