package com.example.laboratorio.security

import android.annotation.SuppressLint
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.example.laboratorio.DemoDataApp
import com.example.laboratorio.data.local.entity.GpsGoogleEntity
import com.example.laboratorio.data.local.entity.GpsSensorsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class GpsCaptureService : Service() {

    companion object {
        private const val CHANNEL_ID = "capture_demodata"
        private const val NOTIF_ID = 100
        private const val INTERVAL_MS = 10_000L
        private const val SENSOR_TIMEOUT_MS = 5_000L
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private val gpsRepo by lazy { (application as DemoDataApp).gpsRepository }
    private var captureJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIF_ID, buildNotification())
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!hasLocationPermission()) {
            stopSelf()
            return START_NOT_STICKY
        }

        if (captureJob == null) {
            captureJob = scope.launch {
                while (isActive) {
                    performCaptures()
                    delay(INTERVAL_MS)
                }
            }
        }
        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private suspend fun performCaptures() {
        val now = System.currentTimeMillis()

        // 1. Captura vía Google FLP (Fused)
        try {
            var flpLoc = fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()

            if (flpLoc == null) {
                flpLoc = fusedClient.lastLocation.await()
            }

            // Guardamos siempre un registro de Google para que la tabla sea simétrica
            gpsRepo.saveGooglePoint(GpsGoogleEntity(
                latitude = flpLoc?.latitude,
                longitude = flpLoc?.longitude,
                accuracy = flpLoc?.accuracy,
                speed = if (flpLoc?.hasSpeed() == true) flpLoc.speed else null,
                bearing = if (flpLoc?.hasBearing() == true) flpLoc.bearing else null,
                timestamp = now
            ))
        } catch (e: Exception) {
            // Si hay un error crítico (ej. Play Services caídos), guardamos registro vacío para no romper la sincronía
            gpsRepo.saveGooglePoint(GpsGoogleEntity(
                latitude = null, longitude = null, accuracy = null, timestamp = now
            ))
        }

        // 2. Captura vía Sensor Hardware (GNSS chip)
        try {
            val sensorLoc = withTimeoutOrNull(SENSOR_TIMEOUT_MS) {
                getRawGpsLocation()
            }

            gpsRepo.saveSensorsPoint(GpsSensorsEntity(
                latitude = sensorLoc?.latitude,
                longitude = sensorLoc?.longitude,
                provider = LocationManager.GPS_PROVIDER,
                altitude = if (sensorLoc?.hasAltitude() == true) sensorLoc.altitude else null,
                timestamp = now
            ))
        } catch (e: Exception) {
            gpsRepo.saveSensorsPoint(GpsSensorsEntity(
                latitude = null, longitude = null, provider = "error", timestamp = now
            ))
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getRawGpsLocation(): android.location.Location? = suspendCancellableCoroutine { cont ->
        if (!locationManager.allProviders.contains(LocationManager.GPS_PROVIDER)) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                locationManager.getCurrentLocation(
                    LocationManager.GPS_PROVIDER,
                    null,
                    ContextCompat.getMainExecutor(this)
                ) { loc ->
                    if (cont.isActive) cont.resume(loc)
                }
            } else {
                // Fallback para API 29 (Android 10)
                val listener = object : android.location.LocationListener {
                    override fun onLocationChanged(l: android.location.Location) {
                        locationManager.removeUpdates(this)
                        if (cont.isActive) cont.resume(l)
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(p: String?, s: Int, e: android.os.Bundle?) {}
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, listener)
                cont.invokeOnCancellation { locationManager.removeUpdates(listener) }
            }
        } catch (e: SecurityException) {
            if (cont.isActive) cont.resume(null)
        }
    }

    private fun hasLocationPermission() = ContextCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Captura DemoData",
            NotificationManager.IMPORTANCE_LOW
        )
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    private fun buildNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("DemoData: captura en curso")
        .setContentText("Comparando Google vs GNSS…")
        .setSmallIcon(android.R.drawable.ic_menu_compass)
        .build()

    override fun onDestroy() {
        super.onDestroy()
        captureJob?.cancel()
        captureJob = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}