package com.example.laboratorio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gps_google")
data class GpsGoogleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val latitude: Double?,
    val longitude: Double?,

    // Metadatos del FLP que NO se obtienen del LocationManager crudo
    val accuracy: Float?,         // precisión horizontal en metros
    val speed: Float? = null,    // m/s, null si no disponible
    val bearing: Float? = null,  // grados desde el norte

    val timestamp: Long          // System.currentTimeMillis() en UTC
)