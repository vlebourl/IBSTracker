package com.tiarkaerell.ibstracker.data.backup

import com.tiarkaerell.ibstracker.data.model.FoodCategory
import com.tiarkaerell.ibstracker.data.model.FoodItem
import com.tiarkaerell.ibstracker.data.model.IBSImpact
import com.tiarkaerell.ibstracker.data.model.Symptom
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date

/**
 * Parses and validates JSON backup files.
 *
 * Handles:
 * - JSON structure validation
 * - Version compatibility checking
 * - Field name mapping (timestamp → date for Symptom)
 * - Type conversion (Long → Date)
 * - Enum parsing (FoodCategory, IBSImpact)
 */
object JsonBackupParser {

    data class ParsedBackup(
        val version: Int,
        val timestamp: String,
        val foodItems: List<FoodItem>,
        val symptoms: List<Symptom>
    )

    /**
     * Parses JSON backup content into domain entities.
     *
     * @param jsonContent Raw JSON string
     * @param currentVersion Current database version for compatibility check
     * @return ParsedBackup with entities, or throws exception if invalid
     * @throws IllegalArgumentException if JSON is malformed or incompatible
     */
    fun parseBackup(jsonContent: String, currentVersion: Int): ParsedBackup {
        try {
            val root = JSONObject(jsonContent)

            // Validate required fields
            if (!root.has("version") || !root.has("foodItems") || !root.has("symptoms")) {
                throw IllegalArgumentException("Missing required fields: version, foodItems, or symptoms")
            }

            val version = root.getInt("version")
            val timestamp = root.optString("timestamp", "")

            // Check version compatibility
            if (version != currentVersion) {
                throw IllegalArgumentException(
                    "Version mismatch: backup is v$version, current database is v$currentVersion"
                )
            }

            // Parse food items
            val foodItemsArray = root.getJSONArray("foodItems")
            val foodItems = parseFoodItems(foodItemsArray)

            // Parse symptoms
            val symptomsArray = root.getJSONArray("symptoms")
            val symptoms = parseSymptoms(symptomsArray)

            return ParsedBackup(
                version = version,
                timestamp = timestamp,
                foodItems = foodItems,
                symptoms = symptoms
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse JSON backup: ${e.message}", e)
        }
    }

    /**
     * Parses food items array from JSON.
     */
    private fun parseFoodItems(array: JSONArray): List<FoodItem> {
        val items = mutableListOf<FoodItem>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            val id = obj.optLong("id", 0L)
            val name = obj.getString("name")
            val quantity = obj.getString("quantity")
            val timestamp = Date(obj.getLong("timestamp"))
            val category = parseFoodCategory(obj.getString("category"))
            val ibsImpacts = parseIbsImpacts(obj.optJSONArray("ibsImpacts"))
            val isCustom = obj.optBoolean("isCustom", true)
            val commonFoodId: Long? = if (obj.has("commonFoodId") && !obj.isNull("commonFoodId")) {
                obj.getInt("commonFoodId").toLong()
            } else {
                null
            }

            items.add(
                FoodItem(
                    id = id,
                    name = name,
                    quantity = quantity,
                    timestamp = timestamp,
                    category = category,
                    ibsImpacts = ibsImpacts,
                    isCustom = isCustom,
                    commonFoodId = commonFoodId
                )
            )
        }

        return items
    }

    /**
     * Parses symptoms array from JSON.
     * Maps "timestamp" field to "date" for compatibility.
     */
    private fun parseSymptoms(array: JSONArray): List<Symptom> {
        val symptoms = mutableListOf<Symptom>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            val id = obj.optInt("id", 0)
            val name = obj.getString("name")
            val intensity = obj.getInt("intensity")
            // Map "timestamp" field to "date"
            val date = Date(obj.getLong("timestamp"))

            symptoms.add(
                Symptom(
                    id = id,
                    name = name,
                    intensity = intensity,
                    date = date
                )
            )
        }

        return symptoms
    }

    /**
     * Parses FoodCategory enum from string.
     */
    private fun parseFoodCategory(value: String): FoodCategory {
        return try {
            FoodCategory.valueOf(value)
        } catch (e: IllegalArgumentException) {
            // Default to OTHER if unknown category
            FoodCategory.OTHER
        }
    }

    /**
     * Parses IBSImpact enum list from JSON array.
     */
    private fun parseIbsImpacts(array: JSONArray?): List<IBSImpact> {
        if (array == null) return emptyList()

        val impacts = mutableListOf<IBSImpact>()
        for (i in 0 until array.length()) {
            val value = array.getString(i)
            try {
                impacts.add(IBSImpact.valueOf(value))
            } catch (e: IllegalArgumentException) {
                // Skip unknown impact types
            }
        }
        return impacts
    }

    /**
     * Validates JSON backup structure without full parsing.
     *
     * @param jsonContent Raw JSON string
     * @return true if valid structure, false otherwise
     */
    fun isValidBackupStructure(jsonContent: String): Boolean {
        return try {
            val root = JSONObject(jsonContent)
            root.has("version") &&
            root.has("foodItems") &&
            root.has("symptoms") &&
            root.getJSONArray("foodItems") != null &&
            root.getJSONArray("symptoms") != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets a preview of the backup content (first few items).
     *
     * @param jsonContent Raw JSON string
     * @param maxItems Maximum number of items to include in preview
     * @return Preview string with item counts and first few items
     */
    fun getBackupPreview(jsonContent: String, maxItems: Int = 3): String {
        return try {
            val root = JSONObject(jsonContent)
            val foodItems = root.getJSONArray("foodItems")
            val symptoms = root.getJSONArray("symptoms")

            val preview = StringBuilder()
            preview.append("📊 Backup contains:\n")
            preview.append("• ${foodItems.length()} food items\n")
            preview.append("• ${symptoms.length()} symptoms\n\n")

            if (foodItems.length() > 0) {
                preview.append("First food items:\n")
                for (i in 0 until minOf(maxItems, foodItems.length())) {
                    val item = foodItems.getJSONObject(i)
                    preview.append("• ${item.getString("name")} (${item.getString("quantity")})\n")
                }
            }

            if (symptoms.length() > 0) {
                preview.append("\nFirst symptoms:\n")
                for (i in 0 until minOf(maxItems, symptoms.length())) {
                    val symptom = symptoms.getJSONObject(i)
                    preview.append("• ${symptom.getString("name")} (intensity: ${symptom.getInt("intensity")})\n")
                }
            }

            preview.toString()
        } catch (e: Exception) {
            "Unable to preview backup: ${e.message}"
        }
    }
}
