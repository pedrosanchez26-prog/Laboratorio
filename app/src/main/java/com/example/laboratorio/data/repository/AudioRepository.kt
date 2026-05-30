package com.example.laboratorio.data.repository

import com.example.laboratorio.data.local.FileStorageManager
import com.example.laboratorio.data.local.dao.AudioDao
import com.example.laboratorio.data.local.entity.AudioEntity
import kotlinx.coroutines.flow.Flow

class AudioRepository(
    private val audioDao: AudioDao,
    private val fileStorage: FileStorageManager
) {
    val allAudios: Flow<List<AudioEntity>> = audioDao.observeAll()
    val count: Flow<Int> = audioDao.observeCount()

    suspend fun registerAudio(
        filePath: String,
        durationMs: Long,
        format: String = "AAC"
    ): Long = audioDao.insert(
        AudioEntity(
            filePath = filePath,
            durationMs = durationMs,
            sizeBytes = fileStorage.fileSize(filePath),
            format = format,
            timestamp = System.currentTimeMillis()
        )
    )

    suspend fun delete(item: AudioEntity) {
        fileStorage.deleteFile(item.filePath)
        audioDao.delete(item)
    }
}