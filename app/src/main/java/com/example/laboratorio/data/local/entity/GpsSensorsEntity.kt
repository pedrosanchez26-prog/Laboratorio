package com.example.laboratorio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gps_sensors")
data class GpsSensorsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val latitude: Double?,
    val longitude: Double?,

    val provider: String,           // "gps", "network" o "passive"
    val altitude: Double? = null,   // metros sobre el nivel del mar
    val satellites: Int? = null,    // satélites usados (si lo expone el OS)

    val timestamp: Long
)