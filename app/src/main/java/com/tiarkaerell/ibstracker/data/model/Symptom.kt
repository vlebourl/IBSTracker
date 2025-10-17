package com.tiarkaerell.ibstracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "symptoms")
data class Symptom(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val intensity: Int,
    val date: Date
)