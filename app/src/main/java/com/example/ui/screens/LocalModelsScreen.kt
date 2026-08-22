package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LocalModelEntity
import com.example.engine.DeviceHardwareAdvisor
import com.example.engine.DeviceHardwareSpecs
import com.example.engine.DownloadProgressState
import com.example.engine.LocalEngineState
import com.example.engine.LocalInferenceStats
import com.example.ui.theme.BentoBackground
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoBorderHighlight
import com.example.ui.theme.BentoError
import com.example.ui.theme.BentoOnPrimaryContainer
import com.example.ui.theme.BentoPrimary
import com.example.ui.theme.BentoPrimaryContainer
import com.example.ui.theme.BentoSecondary
import com.example.ui.theme.BentoSuccess
import com.example.ui.theme.BentoSurface
import com.example.ui.theme.BentoSurfaceVariant
import com.example.ui.theme.BentoTextMuted
import com.example.ui.theme.BentoTextPrimary
import com.example.ui.theme.BentoTextSecondary
import com.example.ui.theme.BentoWarning
import com.example.ui.viewmodel.OmniViewModel

enum class LocalCompatibilityLevel {
    PERFECT,
    SUPPORTED,
    HEAVY,
    NOT_RECOMMENDED
}

@Composable
fun LocalModelsScreen(
    viewModel: OmniViewModel,
    modifier: Modifier = Modifier
) {
    val localModels by viewModel.localModels.collectAsStateWithLifecycle()
    val activeLocalModel by viewModel.activeLocalModel.collectAsStateWithLifecycle()
    val hardwareSpecs by viewModel.deviceHardwareSpecs.collectAsStateWithLifecycle()
    val engineState by viewModel.localEngineState.collectAsStateWithLifecycle()
    val downloadStates by viewModel.downloadStates.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineModeEnabled.collectAsStateWithLifecycle()
    val lastStats by viewModel.lastLocalInferenceStats.collectAsStateWithLifecycle()

    var showAddCustomDialog by remember { mutableStateOf(false) }
    var selectedModelForConfig by remember { mutableStateOf<LocalModelEntity?>(null) }

    // SAF file picker for importing .gguf directly
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast("/") ?: "imported_model.gguf"
            viewModel.importLocalGgufUri(uri, fileName)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Architecture Overview
        item {
            LocalEngineHeader(
                isOfflineMode = isOfflineMode,
                onToggleOffline = { viewModel.toggleOfflineMode(it) },
                onOpenSAF = { filePickerLauncher.launch("*/*") },
                onOpenAddDialog = { showAddCustomDialog = true }
            )
        }

        // 2. Device Hardware Telemetry Card
        item {
            HardwareTelemetryCard(
                specs = hardwareSpecs,
                onRefresh = { viewModel.refreshHardwareSpecs() }
            )
        }

        // 3. Active Engine State / Loaded Model Banner
        item {
            ActiveEngineBanner(
                engineState = engineState,
                activeModel = activeLocalModel,
                lastStats = lastStats,
                onUnload = { viewModel.unloadLocalModelFromRam() }
            )
        }

        // 4. Section Title & Explanation
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Local GGUF Models",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = "On-device quantized LLMs (llama.cpp engine)",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextSecondary
                    )
                }
                Surface(
                    color = BentoPrimaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                ) {
                    Text(
                        text = "${localModels.count { it.isDownloaded }}/${localModels.size} Downloaded",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 5. List of Local GGUF Models
        items(localModels, key = { it.id }) { model ->
            val downloadState = downloadStates[model.id]
            val isLoaded = activeLocalModel?.id == model.id

            LocalModelCard(
                model = model,
                specs = hardwareSpecs,
                isLoaded = isLoaded,
                downloadState = downloadState,
                onDownload = { viewModel.downloadLocalModel(model) },
                onCancelDownload = { viewModel.cancelModelDownload(model.id) },
                onLoadToRam = { viewModel.loadLocalModelToRam(model) },
                onUnload = { viewModel.unloadLocalModelFromRam() },
                onConfigure = { selectedModelForConfig = model },
                onDelete = { viewModel.deleteLocalModel(model.id) }
            )
        }

        // 6. Architecture Explanation Banner
        item {
            ArchitectureInfoBanner()
        }
    }

    // Configure Inference Settings Dialog
    if (selectedModelForConfig != null) {
        LocalModelConfigDialog(
            model = selectedModelForConfig!!,
            onDismiss = { selectedModelForConfig = null },
            onSave = { ctx, temp, topP, gpu, threads ->
                viewModel.updateLocalModelInferenceSettings(
                    id = selectedModelForConfig!!.id,
                    contextLength = ctx,
                    temperature = temp,
                    topP = topP,
                    gpuLayers = gpu,
                    cpuThreads = threads
                )
                selectedModelForConfig = null
            }
        )
    }

    // Add Custom GGUF / Google Drive URL Dialog
    if (showAddCustomDialog) {
        AddCustomGgufDialog(
            onDismiss = { showAddCustomDialog = false },
            onAdd = { name, url, params, quant, minRam, ctx ->
                viewModel.addCustomGgufModel(name, url, params, quant, minRam, ctx)
                showAddCustomDialog = false
            }
        )
    }
}

@Composable
private fun LocalEngineHeader(
    isOfflineMode: Boolean,
    onToggleOffline: (Boolean) -> Unit,
    onOpenSAF: () -> Unit,
    onOpenAddDialog: () -> Unit
) {
    Surface(
        color = BentoSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
        modifier = Modifier.fillMaxWidth().testTag("local_engine_header")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(BentoPrimary, BentoSecondary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "Engine Icon",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Local LLM Inference Engine",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        Text(
                            text = "GGUF + llama.cpp On-Device Runtime",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BentoBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Offline Mode Toggle Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isOfflineMode) BentoPrimary.copy(alpha = 0.08f) else BentoSurfaceVariant)
                    .border(
                        1.dp,
                        if (isOfflineMode) BentoPrimary.copy(alpha = 0.4f) else BentoBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isOfflineMode) Icons.Default.WifiOff else Icons.Default.Security,
                        contentDescription = null,
                        tint = if (isOfflineMode) BentoPrimary else BentoTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOfflineMode) "Strict Offline Mode: ON" else "Hybrid Auto Routing",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOfflineMode) BentoPrimary else BentoTextPrimary
                        )
                        Text(
                            text = if (isOfflineMode) "No cloud APIs used. 100% on-device local execution." else "Cloud fallback when local model is not loaded.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoTextSecondary
                        )
                    }
                }
                Switch(
                    checked = isOfflineMode,
                    onCheckedChange = onToggleOffline,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BentoPrimary,
                        uncheckedThumbColor = BentoTextSecondary,
                        uncheckedTrackColor = BentoSurfaceVariant
                    ),
                    modifier = Modifier.testTag("offline_mode_switch")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons: Import GGUF & Add URL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenSAF,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("import_gguf_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoPrimary)
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import .GGUF", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onOpenAddDialog,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("add_custom_model_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add GGUF / Drive", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun HardwareTelemetryCard(
    specs: DeviceHardwareSpecs,
    onRefresh: () -> Unit
) {
    Surface(
        color = BentoSurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
        modifier = Modifier.fillMaxWidth().testTag("hardware_telemetry_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = BentoSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hardware Telemetry & RAM Advisor",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                }
                IconButton(onClick = onRefresh, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = BentoTextSecondary, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // RAM Usage Progress Bar
            val ramUsedFraction = (specs.ramUsagePercent / 100f).coerceIn(0f, 1f)

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "RAM (Total: ${String.format("%.1f", specs.totalRamGb)} GB)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoTextPrimary
                    )
                    Text(
                        text = "${String.format("%.1f", specs.availableRamGb)} GB Free (${100 - specs.ramUsagePercent}%)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (specs.availableRamGb < 2.0) BentoError else BentoSuccess
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { ramUsedFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (ramUsedFraction > 0.85f) BentoError else if (ramUsedFraction > 0.65f) BentoWarning else BentoPrimary,
                    trackColor = BentoSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Grid with Storage, Cores, ABI
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Storage Free
                Surface(
                    color = BentoSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = BentoTextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Free Disk", style = MaterialTheme.typography.labelSmall, color = BentoTextSecondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format("%.1f", specs.availableStorageGb)} GB",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }
                }

                // CPU Cores
                Surface(
                    color = BentoSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = BentoTextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CPU Cores", style = MaterialTheme.typography.labelSmall, color = BentoTextSecondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${specs.cpuCores} Cores",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                    }
                }

                // Optimal Model Range
                Surface(
                    color = BentoSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Optimal Quant", style = MaterialTheme.typography.labelSmall, color = BentoTextSecondary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = specs.recommendedMaxParameters,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Advisor Note
            Surface(
                color = BentoPrimaryContainer.copy(alpha = 0.35f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BentoPrimary.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = specs.hardwareSummaryText,
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveEngineBanner(
    engineState: LocalEngineState,
    activeModel: LocalModelEntity?,
    lastStats: LocalInferenceStats?,
    onUnload: () -> Unit
) {
    if (activeModel == null && !engineState.isModelLoaded) {
        Surface(
            color = BentoSurfaceVariant,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(BentoTextSecondary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Engine Idle (No Model in RAM)",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoTextSecondary
                        )
                        Text(
                            text = "Select or download a GGUF model below to load into device memory.",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoTextSecondary
                        )
                    }
                }
            }
        }
        return
    }

    Surface(
        color = BentoSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorderHighlight),
        modifier = Modifier.fillMaxWidth().testTag("active_engine_banner")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(BentoSuccess)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Loaded in RAM (Active)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoSuccess
                    )
                }

                OutlinedButton(
                    onClick = onUnload,
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoError)
                ) {
                    Text("Unload RAM", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = activeModel?.displayName ?: (engineState.loadedModel?.displayName ?: "Local Model"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Specs badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgeText(text = "${activeModel?.parameters ?: "3B"} Params", color = BentoPrimary)
                BadgeText(text = activeModel?.quantization ?: "Q4_K_M", color = BentoSecondary)
                BadgeText(text = "Ctx: ${activeModel?.contextLength ?: 2048}", color = BentoTextSecondary)
                BadgeText(text = "${activeModel?.cpuThreads ?: 4} Threads", color = BentoTextSecondary)
            }

            if (lastStats != null) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = BentoBorder)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⚡ Speed: ${lastStats.tokensPerSecond} tokens/sec",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoPrimary
                    )
                    Text(
                        text = "⏱ Latency: ${lastStats.totalTimeMs}ms",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextSecondary
                    )
                    Text(
                        text = "💰 Cost: $0.00 (Local)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoSuccess
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalModelCard(
    model: LocalModelEntity,
    specs: DeviceHardwareSpecs,
    isLoaded: Boolean,
    downloadState: DownloadProgressState?,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onLoadToRam: () -> Unit,
    onUnload: () -> Unit,
    onConfigure: () -> Unit,
    onDelete: () -> Unit
) {
    val (canRun, reason) = DeviceHardwareAdvisor.canSafelyRunModel(model, specs)
    val compLevel = when {
        specs.totalRamGb < model.minRamRequiredGb -> LocalCompatibilityLevel.NOT_RECOMMENDED
        specs.availableRamGb < (model.minRamRequiredGb * 0.6) -> LocalCompatibilityLevel.HEAVY
        specs.totalRamGb >= (model.minRamRequiredGb * 1.4) -> LocalCompatibilityLevel.PERFECT
        else -> LocalCompatibilityLevel.SUPPORTED
    }
    val compLabel = when (compLevel) {
        LocalCompatibilityLevel.PERFECT -> "🟢 Optimal"
        LocalCompatibilityLevel.SUPPORTED -> "🟢 Compatible"
        LocalCompatibilityLevel.HEAVY -> "🟡 Tight RAM"
        LocalCompatibilityLevel.NOT_RECOMMENDED -> "🔴 High RAM"
    }

    val isDownloading = downloadState?.status == "DOWNLOADING" || model.downloadStatus == "DOWNLOADING"
    val progress = downloadState?.progress ?: model.downloadProgress

    Surface(
        color = if (isLoaded) BentoSurfaceVariant else BentoSurface,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isLoaded) BentoPrimary else BentoBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("model_card_${model.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Title + Compatibility Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = model.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoTextPrimary
                        )
                        if (isLoaded) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BentoSuccess.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BentoSuccess)
                            }
                        }
                    }
                    Text(
                        text = "${model.parameters} • ${model.quantization} • ${model.fileSizeFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = BentoTextSecondary
                    )
                }

                // Compatibility Pill
                CompatibilityPill(level = compLevel, label = compLabel)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = model.description,
                style = MaterialTheme.typography.bodySmall,
                color = BentoTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Download Progress Bar if downloading
            if (isDownloading) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (downloadState != null && downloadState.speedMbPerSec > 0) "Streaming GGUF (${String.format("%.1f", downloadState.speedMbPerSec)} MB/s)..." else "Downloading GGUF...",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoPrimary
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = BentoPrimary,
                        trackColor = BentoSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            HorizontalDivider(color = BentoBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary icon buttons (Settings, Delete)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onConfigure,
                        modifier = Modifier.size(32.dp).testTag("config_model_${model.id}")
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = "Tuning", tint = BentoTextSecondary, modifier = Modifier.size(18.dp))
                    }
                    if (model.id.startsWith("local_") || !model.isDownloaded) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp).testTag("delete_model_${model.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = BentoTextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Primary CTA (Download vs Load/Unload)
                if (isDownloading) {
                    OutlinedButton(
                        onClick = onCancelDownload,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoError),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", fontSize = 12.sp)
                    }
                } else if (!model.isDownloaded) {
                    Button(
                        onClick = onDownload,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary),
                        modifier = Modifier.height(34.dp).testTag("download_button_${model.id}")
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download GGUF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isLoaded) {
                    OutlinedButton(
                        onClick = onUnload,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoTextSecondary),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Unload from RAM", fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = onLoadToRam,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoSuccess),
                        modifier = Modifier.height(34.dp).testTag("load_ram_button_${model.id}")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Load to RAM", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompatibilityPill(level: LocalCompatibilityLevel, label: String) {
    val (bgColor, textColor) = when (level) {
        LocalCompatibilityLevel.PERFECT -> Pair(BentoSuccess.copy(alpha = 0.15f), BentoSuccess)
        LocalCompatibilityLevel.SUPPORTED -> Pair(BentoPrimary.copy(alpha = 0.15f), BentoPrimary)
        LocalCompatibilityLevel.HEAVY -> Pair(BentoWarning.copy(alpha = 0.15f), BentoWarning)
        LocalCompatibilityLevel.NOT_RECOMMENDED -> Pair(BentoError.copy(alpha = 0.15f), BentoError)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun BadgeText(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun ArchitectureInfoBanner() {
    Surface(
        color = BentoSurfaceVariant,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = BentoPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "How Local GGUF Inference Works",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "1. GGUF models are stored locally in the app's private sandbox storage.\n" +
                       "2. Google Drive / Hugging Face links act purely as model storage & distribution.\n" +
                       "3. The inference engine loads model weights directly into CPU/GPU RAM using llama.cpp.\n" +
                       "4. Execution requires ZERO internet, zero API cost, and keeps 100% of your data private on device.",
                style = MaterialTheme.typography.bodySmall,
                color = BentoTextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun LocalModelConfigDialog(
    model: LocalModelEntity,
    onDismiss: () -> Unit,
    onSave: (ctx: Int, temp: Float, topP: Float, gpu: Int, threads: Int) -> Unit
) {
    var contextLength by remember { mutableIntStateOf(model.contextLength) }
    var temperature by remember { mutableFloatStateOf(model.temperature) }
    var topP by remember { mutableFloatStateOf(model.topP) }
    var gpuLayers by remember { mutableIntStateOf(model.gpuLayers) }
    var cpuThreads by remember { mutableIntStateOf(model.cpuThreads) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Inference Config: ${model.displayName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Context Length
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Context Window", style = MaterialTheme.typography.bodySmall, color = BentoTextPrimary)
                        Text("$contextLength tokens", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BentoPrimary)
                    }
                    Slider(
                        value = contextLength.toFloat(),
                        onValueChange = { contextLength = it.toInt() },
                        valueRange = 512f..8192f,
                        steps = 7,
                        colors = SliderDefaults.colors(thumbColor = BentoPrimary, activeTrackColor = BentoPrimary)
                    )
                }

                // Temperature
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Temperature (Creativity)", style = MaterialTheme.typography.bodySmall, color = BentoTextPrimary)
                        Text(String.format("%.2f", temperature), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BentoPrimary)
                    }
                    Slider(
                        value = temperature,
                        onValueChange = { temperature = it },
                        valueRange = 0.0f..1.5f,
                        colors = SliderDefaults.colors(thumbColor = BentoPrimary, activeTrackColor = BentoPrimary)
                    )
                }

                // Top-P
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Top-P (Nucleus Sampling)", style = MaterialTheme.typography.bodySmall, color = BentoTextPrimary)
                        Text(String.format("%.2f", topP), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BentoPrimary)
                    }
                    Slider(
                        value = topP,
                        onValueChange = { topP = it },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = BentoPrimary, activeTrackColor = BentoPrimary)
                    )
                }

                // CPU Threads
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("CPU Threads", style = MaterialTheme.typography.bodySmall, color = BentoTextPrimary)
                        Text("$cpuThreads Threads", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BentoPrimary)
                    }
                    Slider(
                        value = cpuThreads.toFloat(),
                        onValueChange = { cpuThreads = it.toInt() },
                        valueRange = 1f..8f,
                        steps = 6,
                        colors = SliderDefaults.colors(thumbColor = BentoPrimary, activeTrackColor = BentoPrimary)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(contextLength, temperature, topP, gpuLayers, cpuThreads) },
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
            ) {
                Text("Save Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = BentoTextSecondary)
            }
        },
        containerColor = BentoSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun AddCustomGgufDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, url: String, params: String, quant: String, minRam: Double, ctx: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var params by remember { mutableStateOf("3B") }
    var quant by remember { mutableStateOf("Q4_K_M") }
    var minRam by remember { mutableStateOf("4.0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Custom GGUF / Google Drive",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = BentoTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Enter a direct HuggingFace download URL or Google Drive shared link. The app will resolve the link and stream the model file to local storage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextSecondary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Model Display Name (e.g. Qwen 2.5 3B)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorder
                    )
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Download URL (HuggingFace / Drive Link)") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPrimary,
                        unfocusedBorderColor = BentoBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = params,
                        onValueChange = { params = it },
                        label = { Text("Params") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoPrimary,
                            unfocusedBorderColor = BentoBorder
                        )
                    )
                    OutlinedTextField(
                        value = quant,
                        onValueChange = { quant = it },
                        label = { Text("Quant") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoPrimary,
                            unfocusedBorderColor = BentoBorder
                        )
                    )
                    OutlinedTextField(
                        value = minRam,
                        onValueChange = { minRam = it },
                        label = { Text("Min RAM (GB)") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BentoPrimary,
                            unfocusedBorderColor = BentoBorder
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && url.isNotBlank()) {
                        val ramDouble = minRam.toDoubleOrNull() ?: 4.0
                        onAdd(name.trim(), url.trim(), params.trim(), quant.trim(), ramDouble, 2048)
                    }
                },
                enabled = name.isNotBlank() && url.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimary)
            ) {
                Text("Add Model")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = BentoTextSecondary)
            }
        },
        containerColor = BentoSurface,
        shape = RoundedCornerShape(16.dp)
    )
}
