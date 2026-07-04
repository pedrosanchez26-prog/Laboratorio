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

import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    private const val ALGORITMO           = "PBKDF2WithHmacSHA256"
    private const val ITERACIONES         = 120_000
    private const val LONGITUD_HASH_BITS  = 256

    fun hash(password: String, salt: ByteArray): String {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            ITERACIONES,
            LONGITUD_HASH_BITS
        )
        val factory = SecretKeyFactory.getInstance(ALGORITMO)
        val bytes   = factory.generateSecret(spec).encoded

        spec.clearPassword()  // Borrado defensivo de la contraseña en memoria

        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].code xor b[i].code)
        }
        return diff == 0
    }
}