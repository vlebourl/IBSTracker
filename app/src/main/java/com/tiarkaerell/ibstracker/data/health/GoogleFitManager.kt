package com.tiarkaerell.ibstracker.data.health

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

class GoogleFitManager(private val context: Context) {
    
    private val _healthData = MutableStateFlow<HealthData?>(null)
    val healthData: StateFlow<HealthData?> = _healthData.asStateFlow()
    
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
        .build()
    
    suspend fun requestPermissions(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
            GoogleSignIn.hasPermissions(account, fitnessOptions)
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun getTodaysHealthData(): Result<HealthData> = withContext(Dispatchers.IO) {
        return@withContext try {
            val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
            
            if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                return@withContext Result.failure(Exception("Google Fit permissions not granted"))
            }
            
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()
            
            // Get step count
            val stepRequest = DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()
            
            val stepResponse = Fitness.getHistoryClient(context, account)
                .readData(stepRequest)
                .await()
            
            val stepCount = stepResponse.buckets
                .flatMap { it.dataSets }
                .flatMap { it.dataPoints }
                .sumOf { it.getValue(Field.FIELD_STEPS).asInt() }
            
            // Get sleep data
            val sleepRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_SLEEP_SEGMENT)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()
            
            val sleepResponse = Fitness.getHistoryClient(context, account)
                .readData(sleepRequest)
                .await()
            
            val sleepMinutes = sleepResponse.dataSets
                .flatMap { it.dataPoints }
                .filter { 
                    val sleepType = it.getValue(Field.FIELD_SLEEP_SEGMENT_TYPE).asInt()
                    // Sleep segment types: 1=AWAKE, 2=SLEEP, 3=OUT_OF_BED, 4=LIGHT_SLEEP, 5=DEEP_SLEEP, 6=REM_SLEEP
                    sleepType == 4 || sleepType == 5 || sleepType == 6 // Light, Deep, REM sleep
                }
                .sumOf { 
                    TimeUnit.MILLISECONDS.toMinutes(
                        it.getEndTime(TimeUnit.MILLISECONDS) - it.getStartTime(TimeUnit.MILLISECONDS)
                    )
                }
            
            // Get heart rate data (average for the day)
            val heartRateRequest = DataReadRequest.Builder()
                .read(DataType.TYPE_HEART_RATE_BPM)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()
            
            val heartRateResponse = Fitness.getHistoryClient(context, account)
                .readData(heartRateRequest)
                .await()
            
            val heartRates = heartRateResponse.dataSets
                .flatMap { it.dataPoints }
                .map { it.getValue(Field.FIELD_BPM).asFloat() }
            
            val averageHeartRate = if (heartRates.isNotEmpty()) {
                heartRates.average().toFloat()
            } else 0f
            
            val healthData = HealthData(
                date = Date(),
                stepCount = stepCount,
                sleepMinutes = sleepMinutes.toInt(),
                averageHeartRate = averageHeartRate,
                stressLevel = calculateStressLevel(averageHeartRate, stepCount)
            )
            
            _healthData.value = healthData
            Result.success(healthData)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getWeeklyHealthData(): Result<List<HealthData>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
            
            if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                return@withContext Result.failure(Exception("Google Fit permissions not granted"))
            }
            
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()
            
            // Get daily step counts for the week
            val stepRequest = DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()
            
            val stepResponse = Fitness.getHistoryClient(context, account)
                .readData(stepRequest)
                .await()
            
            val weeklyData = mutableListOf<HealthData>()
            
            stepResponse.buckets.forEach { bucket ->
                val daySteps = bucket.dataSets
                    .flatMap { it.dataPoints }
                    .sumOf { it.getValue(Field.FIELD_STEPS).asInt() }
                
                val bucketDate = Date(bucket.getStartTime(TimeUnit.MILLISECONDS))
                
                weeklyData.add(
                    HealthData(
                        date = bucketDate,
                        stepCount = daySteps,
                        sleepMinutes = 0, // Would need separate call for each day
                        averageHeartRate = 0f, // Would need separate call for each day
                        stressLevel = StressLevel.NORMAL
                    )
                )
            }
            
            Result.success(weeklyData)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun calculateStressLevel(heartRate: Float, stepCount: Int): StressLevel {
        return when {
            heartRate == 0f -> StressLevel.UNKNOWN
            heartRate > 100 && stepCount < 5000 -> StressLevel.HIGH
            heartRate > 80 && stepCount < 8000 -> StressLevel.MODERATE
            else -> StressLevel.NORMAL
        }
    }
    
    data class HealthData(
        val date: Date,
        val stepCount: Int,
        val sleepMinutes: Int,
        val averageHeartRate: Float,
        val stressLevel: StressLevel
    )
    
    enum class StressLevel {
        UNKNOWN, NORMAL, MODERATE, HIGH
    }
}