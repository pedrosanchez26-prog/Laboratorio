package com.example.laboratorio.data.repository

import com.example.laboratorio.data.local.dao.GpsGoogleDao
import com.example.laboratorio.data.local.dao.GpsSensorsDao
import com.example.laboratorio.data.local.entity.GpsGoogleEntity
import com.example.laboratorio.data.local.entity.GpsSensorsEntity
import kotlinx.coroutines.flow.Flow

class GpsRepository(
    private val googleDao: GpsGoogleDao,
    private val sensorsDao: GpsSensorsDao
) {
    // ── Streams reactivos para la UI ──
    val googlePoints: Flow<List<GpsGoogleEntity>> = googleDao.observeAll()
    val sensorsPoints: Flow<List<GpsSensorsEntity>> = sensorsDao.observeAll()

    val googleCount: Flow<Int> = googleDao.observeCount()
    val sensorsCount: Flow<Int> = sensorsDao.observeCount()

    // ── Mutaciones (llamadas desde el servicio de captura) ──
    suspend fun saveGooglePoint(point: GpsGoogleEntity) = googleDao.insert(point)
    suspend fun saveSensorsPoint(point: GpsSensorsEntity) = sensorsDao.insert(point)

    suspend fun clearAll() {
        googleDao.deleteAll()
        sensorsDao.deleteAll()
    }
}