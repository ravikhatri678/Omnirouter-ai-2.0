package com.example.engine

import android.content.Context
import android.net.Uri
import com.example.data.model.LocalModelEntity
import com.example.network.NetworkClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

data class DownloadProgressState(
    val modelId: String,
    val bytesRead: Long = 0L,
    val totalBytes: Long = 0L,
    val progress: Float = 0f,
    val speedMbPerSec: Double = 0.0,
    val status: String = "IDLE", // IDLE, DOWNLOADING, COMPLETED, FAILED, CANCELLED
    val errorMessage: String? = null
)

object LocalModelDownloadManager {

    private val client = NetworkClient.okHttpClient
    private val activeDownloadJobs = ConcurrentHashMap<String, Job>()
    
    private val _downloadStates = MutableStateFlow<Map<String, DownloadProgressState>>(emptyMap())
    val downloadStates: StateFlow<Map<String, DownloadProgressState>> = _downloadStates.asStateFlow()

    /**
     * Converts a Google Drive share link into a direct streaming download link.
     */
    fun resolveDownloadUrl(rawUrl: String): String {
        val trimmed = rawUrl.trim()
        
        // Check for Google Drive file link: drive.google.com/file/d/<FILE_ID>/view
        val driveFileRegex = Regex("drive\\.google\\.com/file/d/([a-zA-Z0-9_-]+)")
        val match1 = driveFileRegex.find(trimmed)
        if (match1 != null) {
            val fileId = match1.groupValues[1]
            return "https://drive.google.com/uc?export=download&id=$fileId&confirm=t"
        }

        // Check for drive.google.com/open?id=<FILE_ID> or id=<FILE_ID>
        val driveIdRegex = Regex("[?&]id=([a-zA-Z0-9_-]+)")
        val match2 = driveIdRegex.find(trimmed)
        if (match2 != null && trimmed.contains("drive.google.com")) {
            val fileId = match2.groupValues[1]
            return "https://drive.google.com/uc?export=download&id=$fileId&confirm=t"
        }

        // HuggingFace / Direct URL
        return trimmed
    }

    suspend fun startDownload(
        context: Context,
        model: LocalModelEntity,
        onProgressUpdate: suspend (Float, Long, String) -> Unit,
        onComplete: suspend (File) -> Unit,
        onError: suspend (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        val directUrl = resolveDownloadUrl(model.downloadUrl)
        if (directUrl.isBlank()) {
            onError("Download URL is empty. Please provide a direct GGUF URL or Google Drive link.")
            return@withContext
        }

        val targetDir = DeviceHardwareAdvisor.getAIModelsDirectory(context)
        val targetFile = File(targetDir, model.fileName)
        val tempFile = File(targetDir, "${model.fileName}.tmp")

        updateState(model.id, DownloadProgressState(modelId = model.id, status = "DOWNLOADING"))

        try {
            val request = Request.Builder()
                .url(directUrl)
                .addHeader("User-Agent", "OmniRouter-AI-Engine/1.0 (Android)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val err = "Download failed: HTTP ${response.code} (${response.message})"
                updateState(model.id, DownloadProgressState(modelId = model.id, status = "FAILED", errorMessage = err))
                onError(err)
                return@withContext
            }

            val body = response.body
            if (body == null) {
                val err = "Empty response body from server."
                updateState(model.id, DownloadProgressState(modelId = model.id, status = "FAILED", errorMessage = err))
                onError(err)
                return@withContext
            }

            val totalBytes = body.contentLength().takeIf { it > 0 } ?: model.fileSizeBytes.takeIf { it > 0 } ?: (2L * 1024 * 1024 * 1024)
            var bytesRead = 0L
            var startTime = System.currentTimeMillis()
            var lastUpdateTime = startTime

            body.byteStream().use { input ->
                FileOutputStream(tempFile).use { output ->
                    val buffer = ByteArray(64 * 1024) // 64 KB buffer
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        bytesRead += read

                        val now = System.currentTimeMillis()
                        if (now - lastUpdateTime >= 250 || bytesRead == totalBytes) {
                            val elapsedSec = (now - startTime) / 1000.0
                            val speedMb = if (elapsedSec > 0) (bytesRead / (1024.0 * 1024.0)) / elapsedSec else 0.0
                            val progress = (bytesRead.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)

                            val progressState = DownloadProgressState(
                                modelId = model.id,
                                bytesRead = bytesRead,
                                totalBytes = totalBytes,
                                progress = progress,
                                speedMbPerSec = speedMb,
                                status = "DOWNLOADING"
                            )
                            updateState(model.id, progressState)
                            onProgressUpdate(progress, bytesRead, "DOWNLOADING")
                            lastUpdateTime = now
                        }
                    }
                    output.flush()
                }
            }

            // Rename temp to target
            if (targetFile.exists()) targetFile.delete()
            tempFile.renameTo(targetFile)

            updateState(
                model.id,
                DownloadProgressState(
                    modelId = model.id,
                    bytesRead = bytesRead,
                    totalBytes = bytesRead,
                    progress = 1.0f,
                    status = "COMPLETED"
                )
            )
            onProgressUpdate(1.0f, bytesRead, "READY")
            onComplete(targetFile)

        } catch (e: CancellationException) {
            tempFile.delete()
            updateState(model.id, DownloadProgressState(modelId = model.id, status = "CANCELLED"))
            onError("Download cancelled.")
        } catch (e: Exception) {
            tempFile.delete()
            val msg = e.localizedMessage ?: "Unknown download error"
            updateState(model.id, DownloadProgressState(modelId = model.id, status = "FAILED", errorMessage = msg))
            onError(msg)
        }
    }

    fun cancelDownload(modelId: String) {
        activeDownloadJobs[modelId]?.cancel()
        activeDownloadJobs.remove(modelId)
        updateState(modelId, DownloadProgressState(modelId = modelId, status = "CANCELLED"))
    }

    suspend fun importLocalFile(
        context: Context,
        sourceUri: Uri,
        customName: String
    ): Pair<LocalModelEntity, File> = withContext(Dispatchers.IO) {
        val contentResolver = context.contentResolver
        val targetDir = DeviceHardwareAdvisor.getAIModelsDirectory(context)
        
        val safeFileName = if (customName.endsWith(".gguf", ignoreCase = true)) {
            customName
        } else {
            "${customName.lowercase().replace(" ", "_")}.gguf"
        }
        val targetFile = File(targetDir, safeFileName)

        contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        } ?: throw IllegalStateException("Unable to read selected file stream.")

        val sizeBytes = targetFile.length()
        val sizeFormatted = String.format("%.1f GB", sizeBytes.toDouble() / (1024 * 1024 * 1024))
        val inferredParam = when {
            sizeBytes < 1.5e9 -> "1.5B"
            sizeBytes < 3.5e9 -> "3B-4B"
            sizeBytes < 6.0e9 -> "7B-8B"
            else -> "14B+"
        }

        val entity = LocalModelEntity(
            id = "local_${System.currentTimeMillis()}",
            displayName = customName.removeSuffix(".gguf").replace("_", " ").replace("-", " ").capitalizeWords(),
            fileName = safeFileName,
            modelFormat = "GGUF",
            parameters = inferredParam,
            quantization = "Q4_K_M",
            fileSizeFormatted = sizeFormatted,
            fileSizeBytes = sizeBytes,
            localFilePath = targetFile.absolutePath,
            downloadUrl = "",
            isDownloaded = true,
            downloadProgress = 1f,
            downloadStatus = "READY",
            minRamRequiredGb = if (sizeBytes < 2e9) 4.0 else if (sizeBytes < 4e9) 6.0 else 8.0,
            recommendedRamGb = if (sizeBytes < 2e9) 4.0 else if (sizeBytes < 4e9) 6.0 else 8.0,
            description = "Custom imported GGUF model from device storage."
        )

        Pair(entity, targetFile)
    }

    private fun updateState(modelId: String, state: DownloadProgressState) {
        val current = _downloadStates.value.toMutableMap()
        current[modelId] = state
        _downloadStates.value = current
    }

    private fun String.capitalizeWords(): String =
        split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}
