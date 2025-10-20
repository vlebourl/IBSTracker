package com.tiarkaerell.ibstracker.data.sync

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.tiarkaerell.ibstracker.data.database.AppDatabase
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.model.Symptom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class GoogleDriveBackup(
    private val context: Context,
    private val database: AppDatabase
) {
    
    private val json = Json { prettyPrint = true }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
    
    @Serializable
    data class BackupData(
        val version: Int = 1,
        val timestamp: String,
        val foodItems: List<SerializableFoodItem>,
        val symptoms: List<SerializableSymptom>
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
    
    suspend fun createBackup(): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
                ?: return@withContext Result.failure(Exception("Not signed in to Google"))
            
            val driveService = getDriveService(account)
                ?: return@withContext Result.failure(Exception("Failed to initialize Drive service"))
            
            // Get data from database
            val foodItems = database.foodItemDao().getAllFoodItems()
            val symptoms = database.symptomDao().getAllSymptoms()
            
            // Convert to serializable format
            val backupData = BackupData(
                timestamp = dateFormat.format(Date()),
                foodItems = foodItems.map { 
                    SerializableFoodItem(
                        id = it.id,
                        name = it.name,
                        quantity = it.quantity,
                        category = it.category.name,
                        timestamp = it.date.time
                    )
                },
                symptoms = symptoms.map {
                    SerializableSymptom(
                        id = it.id,
                        name = it.name,
                        intensity = it.intensity,
                        timestamp = it.date.time
                    )
                }
            )
            
            // Convert to JSON
            val jsonContent = json.encodeToString(backupData)
            
            // Create file metadata
            val fileName = "ibs_tracker_backup_${backupData.timestamp}.json"
            val fileMetadata = File()
                .setName(fileName)
                .setParents(listOf(getAppFolderId(driveService)))
            
            // Upload to Drive
            val fileContent = com.google.api.client.http.ByteArrayContent.fromString("application/json", jsonContent)
            val uploadedFile = driveService.files().create(fileMetadata, fileContent)
                .setFields("id")
                .execute()
            
            Result.success("Backup created: ${uploadedFile.id}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun restoreFromBackup(fileId: String): Result<String> = withContext(Dispatchers.IO) {
        return@withContext try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
                ?: return@withContext Result.failure(Exception("Not signed in to Google"))
            
            val driveService = getDriveService(account)
                ?: return@withContext Result.failure(Exception("Failed to initialize Drive service"))
            
            // Download file content
            val outputStream = ByteArrayOutputStream()
            driveService.files().get(fileId).executeMediaAndDownloadTo(outputStream)
            val jsonContent = outputStream.toString("UTF-8")
            
            // Parse backup data
            val backupData = json.decodeFromString<BackupData>(jsonContent)
            
            // Clear existing data
            database.foodItemDao().deleteAllFoodItems()
            database.symptomDao().deleteAllSymptoms()
            
            // Restore food items
            backupData.foodItems.forEach { item ->
                val foodItem = FoodItem(
                    id = 0, // Let Room auto-generate new IDs
                    name = item.name,
                    quantity = item.quantity,
                    category = com.tiarkaerell.ibstracker.data.model.FoodCategory.valueOf(item.category),
                    date = Date(item.timestamp)
                )
                database.foodItemDao().insertFoodItem(foodItem)
            }
            
            // Restore symptoms
            backupData.symptoms.forEach { item ->
                val symptom = Symptom(
                    id = 0, // Let Room auto-generate new IDs
                    name = item.name,
                    intensity = item.intensity,
                    date = Date(item.timestamp)
                )
                database.symptomDao().insertSymptom(symptom)
            }
            
            Result.success("Restored ${backupData.foodItems.size} food items and ${backupData.symptoms.size} symptoms")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun listBackups(): Result<List<DriveFile>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
                ?: return@withContext Result.failure(Exception("Not signed in to Google"))
            
            val driveService = getDriveService(account)
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
    
    private fun getDriveService(account: GoogleSignInAccount): Drive? {
        return try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                setOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account
            
            Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                credential
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
}