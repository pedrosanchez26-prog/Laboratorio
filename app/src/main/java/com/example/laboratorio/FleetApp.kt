package com.example.laboratorio

import android.app.Application
import com.example.laboratorio.data.local.FileStorageManager
import com.example.laboratorio.data.local.DemoDataDatabase
import com.example.laboratorio.data.repository.AudioRepository
import com.example.laboratorio.data.repository.GpsRepository
import com.example.laboratorio.data.repository.MediaRepository
import com.example.laboratorio.data.session.SessionManager

class DemoDataApp : Application() {

    // Inicialización perezosa: solo se crea al primer acceso
    val database by lazy { DemoDataDatabase.getInstance(this) }
    val fileStorage by lazy { FileStorageManager(this) }
    val sessionManager by lazy { SessionManager(this) }

    val gpsRepository by lazy {
        GpsRepository(database.gpsGoogleDao(), database.gpsSensorsDao())
    }
    val mediaRepository by lazy {
        MediaRepository(database.mediaDao(), fileStorage)
    }
    val audioRepository by lazy {
        AudioRepository(database.audioDao(), fileStorage)
    }
}