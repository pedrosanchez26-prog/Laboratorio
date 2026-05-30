package com.example.laboratorio.data.repository

import com.example.laboratorio.data.local.FileStorageManager
import com.example.laboratorio.data.local.dao.MediaDao
import com.example.laboratorio.data.local.entity.MediaEntity
import com.example.laboratorio.data.local.entity.MediaType
import kotlinx.coroutines.flow.Flow

class MediaRepository(
    private val mediaDao: MediaDao,
    private val fileStorage: FileStorageManager
) {
    val allMedia: Flow<List<MediaEntity>> = mediaDao.observeAll()
    val photoCount: Flow<Int> = mediaDao.observePhotoCount()
    val videoCount: Flow<Int> = mediaDao.observeVideoCount()

    /**
     * Registra una foto ya guardada en disco. La pantalla de cámara llama
     * a fileStorage.newPhotoFile() antes para obtener el path destino,
     * captura ahí, y luego invoca este método con el path resultante.
     */
    suspend fun registerPhoto(
        filePath: String,
        widthPx: Int,
        heightPx: Int
    ): Long = mediaDao.insert(
        MediaEntity(
            filePath = filePath,
            type = MediaType.PHOTO.name,
            sizeBytes = fileStorage.fileSize(filePath),
            widthPx = widthPx,
            heightPx = heightPx,
            timestamp = System.currentTimeMillis()
        )
    )

    suspend fun registerVideo(
        filePath: String,
        durationMs: Long
    ): Long = mediaDao.insert(
        MediaEntity(
            filePath = filePath,
            type = MediaType.VIDEO.name,
            sizeBytes = fileStorage.fileSize(filePath),
            durationMs = durationMs,
            timestamp = System.currentTimeMillis()
        )
    )

    /** Borra el archivo físico Y el registro en Room en una sola operación. */
    suspend fun delete(item: MediaEntity) {
        fileStorage.deleteFile(item.filePath)
        mediaDao.delete(item)
    }
}