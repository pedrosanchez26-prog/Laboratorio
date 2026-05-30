package com.example.laboratorio.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class AudioRecorderManager(private val context: Context) {

    private var recorder: MediaRecorder? = null

    /**
     * Crea un archivo vacío con un nombre único basado en el tiempo actual
     * dentro del directorio privado de la app (no requiere permisos externos).
     */
    fun createAudioFile(): File {
        val audioDir = File(context.filesDir, "audios").apply {
            if (!exists()) mkdirs()
        }
        return File(audioDir, "AUDIO_${System.currentTimeMillis()}.m4a")
    }

    /**
     * Inicializa y arranca la grabación de audio desde el micrófono.
     */
    fun start(outputFile: File) {
        // Evita fugas de memoria si ya había una grabación activa
        stop()

        // MediaRecorder requiere una inicialización distinta a partir de Android 11 (API 30+)
        val createRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        recorder = createRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.absolutePath)

            try {
                prepare()
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Detiene la grabación y libera los recursos del hardware del micrófono.
     */
    fun stop() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // Maneja el caso donde se llama a stop() inmediatamente después de start()
            e.printStackTrace()
        } finally {
            recorder = null
        }
    }

    /**
     * Extrae la duración exacta en milisegundos de un archivo de audio ya grabado.
     */
    fun getDuration(file: File): Long {
        if (!file.exists()) return 0L
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        } finally {
            retriever.release()
        }
    }
}