package com.example.engine

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import com.example.data.model.LocalModelEntity
import java.io.File

data class DeviceHardwareSpecs(
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val totalRamGb: Double,
    val availableRamGb: Double,
    val ramUsagePercent: Int,
    val cpuCores: Int,
    val availableStorageGb: Double,
    val recommendedMaxParameters: String,
    val hardwareTierLabel: String,
    val hardwareSummaryText: String
)

object DeviceHardwareAdvisor {

    fun getDeviceSpecs(context: Context): DeviceHardwareSpecs {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val totalRam = memoryInfo.totalMem.takeIf { it > 0 } ?: (6L * 1024 * 1024 * 1024) // Default fallback 6GB
        val availableRam = memoryInfo.availMem.takeIf { it > 0 } ?: (3L * 1024 * 1024 * 1024)

        val rawTotal = totalRam.toDouble() / (1024.0 * 1024.0 * 1024.0)
        val rawAvail = availableRam.toDouble() / (1024.0 * 1024.0 * 1024.0)
        val totalGb = Math.round(rawTotal * 10.0) / 10.0
        val availGb = Math.round(rawAvail * 10.0) / 10.0
        val usedPercent = (((totalRam - availableRam).toDouble() / totalRam.toDouble()) * 100).toInt().coerceIn(0, 100)

        val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(4)

        // Storage space in Internal / AIModels folder
        var availStorageGb = 16.0
        try {
            val stat = StatFs(context.filesDir.absolutePath)
            val bytesAvailable = stat.availableBlocksLong * stat.blockSizeLong
            val rawStorage = bytesAvailable.toDouble() / (1024.0 * 1024.0 * 1024.0)
            availStorageGb = Math.round(rawStorage * 10.0) / 10.0
        } catch (_: Exception) {}

        val (maxParam, tierLabel, summary) = when {
            totalGb < 5.0 -> Triple(
                "1–2B Quantized (Q4_K_M)",
                "4 GB RAM • Entry Mobile Tier",
                "Practical starting point: ~1–2B quantized models (Qwen 2.5 1.5B, DeepSeek R1 1.5B). Keeps ~1.5 GB free for Android OS stability."
            )
            totalGb < 7.5 -> Triple(
                "2–4B Quantized (Q4_K_M)",
                "6 GB RAM • Balanced Mobile Tier",
                "Practical starting point: ~2–4B quantized models (Qwen 3 4B, Llama 3.2 3B). High speed with deep reasoning."
            )
            totalGb < 11.5 -> Triple(
                "4–7B Quantized (Q4_K_M)",
                "8 GB RAM • High Performance Tier",
                "Practical starting point: ~4–7B quantized models (Mistral 7B, Qwen 7B). Desktop-grade reasoning capabilities on device."
            )
            else -> Triple(
                "7–8B+ Quantized (Q4_K_M / Q8)",
                "12 GB+ RAM • Flagship Frontier Tier",
                "Practical starting point: ~7–8B+ quantized models (Llama 3.3 8B, Qwen 14B Q4) comfortably loaded in RAM."
            )
        }

        return DeviceHardwareSpecs(
            totalRamBytes = totalRam,
            availableRamBytes = availableRam,
            totalRamGb = totalGb,
            availableRamGb = availGb,
            ramUsagePercent = usedPercent,
            cpuCores = cores,
            availableStorageGb = availStorageGb,
            recommendedMaxParameters = maxParam,
            hardwareTierLabel = tierLabel,
            hardwareSummaryText = summary
        )
    }

    fun canSafelyRunModel(model: LocalModelEntity, specs: DeviceHardwareSpecs): Pair<Boolean, String> {
        if (specs.totalRamGb < model.minRamRequiredGb) {
            return Pair(
                false,
                "Device has ${specs.totalRamGb} GB RAM. This model requires minimum ${model.minRamRequiredGb} GB RAM."
            )
        }
        if (specs.availableRamGb < (model.minRamRequiredGb * 0.55)) {
            return Pair(
                true,
                "RAM is tight (${specs.availableRamGb} GB available). Consider closing background apps before loading."
            )
        }
        return Pair(true, "Compatible with device (${specs.totalRamGb} GB total RAM).")
    }

    fun getAIModelsDirectory(context: Context): File {
        // App private files directory is always accessible without dangerous runtime permissions
        val internalModelsDir = File(context.filesDir, "AIModels")
        if (!internalModelsDir.exists()) {
            internalModelsDir.mkdirs()
        }
        return internalModelsDir
    }
}
