package com.tiarkaerell.ibstracker.data.sync

import android.content.Context
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.model.Symptom
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * JsonExportImport - Smart Food Categorization System (v1.9.0)
 *
 * Manual JSON export/import utility for local backup/restore.
 *
 * Purpose (FR-049):
 * - Provides manual data backup option independent of cloud services
 * - Enables data portability and manual recovery
 * - Useful for device transfers, troubleshooting, data audits
 *
 * Security:
 * - Files stored in app-private directory (not world-readable)
 * - No encryption (user's device is trusted)
 * - File access requires user consent (SAF integration)
 *
 * Usage:
 * ```kotlin
 * // Export
 * val exportFile = JsonExportImport.exportToJson(context, database)
 * // File location: /data/data/com.tiarkaerell.ibstracker/files/backups/backup_20250121_143052.json
 *
 * // Import
 * val result = JsonExportImport.importFromJson(context, database, exportFile)
 * ```
 */
object JsonExportImport {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    /**
     * Serializable data structure for JSON export/import
     */
    @Serializable
    data class BackupData(
        val version: Int,
        val timestamp: String,
        val foodItems: List<SerializableFoodItem>,
        val symptoms: List<SerializableSymptom>
    )

    @Serializable
    data class SerializableFoodItem(
        val id: Long,
        val name: String,
        val quantity: String,
        val timestamp: Long,
        val category: String,
        val ibsImpacts: List<String>,
        val isCustom: Boolean,
        val commonFoodId: Long?
    )

    @Serializable
    data class SerializableSymptom(
        val id: Int,
        val name: String,
        val intensity: Int,
        val timestamp: Long
    )

    /**
     * Export all data to JSON file.
     *
     * @param context Application context
     * @param database Room database instance
     * @return File object pointing to exported JSON file
     * @throws Exception if export fails (IOException, database errors, etc.)
     */
    suspend fun exportToJson(context: Context, database: AppDatabase): File {
        // Get all data from database
        val foodItems = database.foodItemDao().getAllFoodItems().first()
        val symptoms = database.symptomDao().getAll().first()

        // Convert to serializable format
        val backupData = BackupData(
            version = 9,
            timestamp = dateFormat.format(Date()),
            foodItems = foodItems.map { food ->
                SerializableFoodItem(
                    id = food.id,
                    name = food.name,
                    quantity = food.quantity,
                    timestamp = food.timestamp.time,
                    category = food.category.name,
                    ibsImpacts = food.ibsImpacts.map { it.name },
                    isCustom = food.isCustom,
                    commonFoodId = food.commonFoodId
                )
            },
            symptoms = symptoms.map { symptom ->
                SerializableSymptom(
                    id = symptom.id,
                    name = symptom.name,
                    intensity = symptom.intensity,
                    timestamp = symptom.date.time
                )
            }
        )

        // Serialize to JSON
        val jsonString = json.encodeToString(BackupData.serializer(), backupData)

        // Write to file in app-private storage
        val backupsDir = File(context.filesDir, "backups")
        backupsDir.mkdirs()

        val filename = "backup_${dateFormat.format(Date())}.json"
        val file = File(backupsDir, filename)
        file.writeText(jsonString)

        return file
    }

    /**
     * Import data from JSON file.
     *
     * @param context Application context
     * @param database Room database instance
     * @param file JSON file to import
     * @param clearExisting If true, clears existing data before import (default: false)
     * @return Result with success message or error
     * @throws Exception if import fails (malformed JSON, database errors, etc.)
     */
    suspend fun importFromJson(
        context: Context,
        database: AppDatabase,
        file: File,
        clearExisting: Boolean = false
    ): Result<String> {
        return try {
            // Read and parse JSON file
            val jsonString = file.readText()
            val backupData = json.decodeFromString(BackupData.serializer(), jsonString)

            // Validate version compatibility
            if (backupData.version != 9) {
                return Result.failure(Exception("Backup version ${backupData.version} is not compatible with current database version 9"))
            }

            // Clear existing data if requested
            if (clearExisting) {
                database.foodItemDao().deleteAll()
                database.symptomDao().deleteAllSymptoms()
            }

            // Import food items
            var importedFoodCount = 0
            backupData.foodItems.forEach { item ->
                try {
                    val foodItem = FoodItem(
                        id = 0, // Let Room auto-generate new IDs to avoid conflicts
                        name = item.name,
                        quantity = item.quantity,
                        timestamp = Date(item.timestamp),
                        category = com.tiarkaerell.ibstracker.data.model.FoodCategory.valueOf(item.category),
                        ibsImpacts = item.ibsImpacts.mapNotNull { impactName ->
                            try {
                                com.tiarkaerell.ibstracker.data.model.IBSImpact.valueOf(impactName)
                            } catch (e: IllegalArgumentException) {
                                null // Skip invalid impact names
                            }
                        },
                        isCustom = item.isCustom,
                        commonFoodId = item.commonFoodId
                    )
                    database.foodItemDao().insert(foodItem)
                    importedFoodCount++
                } catch (e: Exception) {
                    // Log error but continue importing other items
                    android.util.Log.e("JsonExportImport", "Failed to import food item: ${item.name}", e)
                }
            }

            // Import symptoms
            var importedSymptomCount = 0
            backupData.symptoms.forEach { item ->
                try {
                    val symptom = Symptom(
                        id = 0, // Let Room auto-generate new IDs
                        name = item.name,
                        intensity = item.intensity,
                        date = Date(item.timestamp)
                    )
                    database.symptomDao().insert(symptom)
                    importedSymptomCount++
                } catch (e: Exception) {
                    android.util.Log.e("JsonExportImport", "Failed to import symptom: ${item.name}", e)
                }
            }

            Result.success("Imported $importedFoodCount food items and $importedSymptomCount symptoms from ${backupData.timestamp}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * List all available backup files.
     *
     * @param context Application context
     * @return List of backup files sorted by date (newest first)
     */
    fun listBackups(context: Context): List<File> {
        val backupsDir = File(context.filesDir, "backups")
        if (!backupsDir.exists()) {
            return emptyList()
        }

        return backupsDir.listFiles { file ->
            file.name.endsWith(".json") && file.name.startsWith("backup_")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /**
     * Delete a backup file.
     *
     * @param file Backup file to delete
     * @return true if deleted successfully
     */
    fun deleteBackup(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get backup file info.
     *
     * @param file Backup file
     * @return Human-readable info string
     */
    fun getBackupInfo(file: File): String {
        val fileSize = file.length() / 1024.0 // KB
        val date = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(file.lastModified()))
        return "$date (${String.format("%.1f", fileSize)} KB)"
    }
}
