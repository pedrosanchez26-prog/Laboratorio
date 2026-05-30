package com.example.laboratorio.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.laboratorio.data.local.FileStorageManager
import com.example.laboratorio.data.local.entity.MediaEntity
import com.example.laboratorio.data.repository.MediaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MediaViewModel(
    private val mediaRepository: MediaRepository,
    private val fileStorage: FileStorageManager
) : ViewModel() {

    // ── Streams reactivos para la UI ──
    val mediaList = mediaRepository.allMedia.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val photoCount = mediaRepository.photoCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        0
    )

    val videoCount = mediaRepository.videoCount.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        0
    )

    // ── Helpers para que la pantalla pida un archivo destino antes de capturar ──
    // (CameraX necesita conocer el File de destino al construir el OutputFileOptions)
    fun newPhotoFile(): File = fileStorage.newPhotoFile()
    fun newVideoFile(): File = fileStorage.newVideoFile()

    // ── Callbacks tras una captura exitosa ──
    /**
     * Llamado por la UI cuando CameraX confirma que la foto se guardó
     * correctamente en disco. Aquí registramos la metadata en Room.
     */
    fun onPhotoCaptured(filePath: String, widthPx: Int, heightPx: Int) {
        viewModelScope.launch {
            mediaRepository.registerPhoto(filePath, widthPx, heightPx)
        }
    }

    /**
     * Llamado al detener la grabación de video. Compose obtiene la duración
     * desde el evento Finalize de VideoCapture y la pasa aquí.
     */
    fun onVideoCaptured(filePath: String, durationMs: Long) {
        viewModelScope.launch {
            mediaRepository.registerVideo(filePath, durationMs)
        }
    }

    // ── Eliminación coordinada archivo + Room ──
    fun delete(item: MediaEntity) {
        viewModelScope.launch {
            mediaRepository.delete(item)
        }
    }

    // ── Factory para inyección manual desde @Composable ──
    class Factory(
        private val mediaRepository: MediaRepository,
        private val fileStorage: FileStorageManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MediaViewModel(mediaRepository, fileStorage) as T
    }
}