package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ChangeLogCategory
import com.example.data.model.ChangeLogEntryEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSessionEntity
import com.example.data.model.LocalModelEntity
import com.example.data.model.ModelConfigEntity
import com.example.data.model.ModelTier
import com.example.data.model.ProviderEntity
import com.example.data.model.QualityPreference
import com.example.data.model.RoutingRuleEntity
import com.example.data.model.TaskType
import com.example.data.model.UsageLogEntity
import com.example.data.repository.OmniRepository
import com.example.engine.ChangeLogManager
import com.example.engine.DeviceHardwareAdvisor
import com.example.engine.DeviceHardwareSpecs
import com.example.engine.DownloadProgressState
import com.example.engine.LocalEngineState
import com.example.engine.LocalInferenceStats
import com.example.engine.LocalLlmInferenceEngine
import com.example.engine.LocalModelDownloadManager
import com.example.engine.ModelRouterEngine
import com.example.engine.RoutingDecision
import com.example.network.AiApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class PromptIntentPreview(
    val detectedTask: TaskType,
    val complexityScore: Int,
    val recommendedModelName: String,
    val markers: List<String>
)

data class DuelResponseState(
    val isDuelMode: Boolean = false,
    val modelA: ModelConfigEntity? = null,
    val modelB: ModelConfigEntity? = null,
    val responseA: ChatMessageEntity? = null,
    val responseB: ChatMessageEntity? = null,
    val isLoadingA: Boolean = false,
    val isLoadingB: Boolean = false
)

class OmniViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = OmniRepository(
        providerDao = db.providerDao(),
        modelConfigDao = db.modelConfigDao(),
        routingRuleDao = db.routingRuleDao(),
        chatDao = db.chatDao(),
        usageLogDao = db.usageLogDao(),
        changeLogDao = db.changeLogDao(),
        localModelDao = db.localModelDao()
    )
    private val apiService = AiApiService()

    // Session State
    private val _currentSessionId = MutableStateFlow("session_default")
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    val currentMessages: StateFlow<List<ChatMessageEntity>> = _currentSessionId
        .combine(repository.allSessions) { id, _ -> id }
        .combine(db.chatDao().getMessagesForSessionFlow(_currentSessionId.value)) { _, msgs -> msgs }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Routing Settings
    private val _qualityPreference = MutableStateFlow(QualityPreference.AUTO)
    val qualityPreference: StateFlow<QualityPreference> = _qualityPreference.asStateFlow()

    private val _manualOverrideModel = MutableStateFlow<ModelConfigEntity?>(null)
    val manualOverrideModel: StateFlow<ModelConfigEntity?> = _manualOverrideModel.asStateFlow()

    // Offline & Local Engine States
    private val _isOfflineModeEnabled = MutableStateFlow(false)
    val isOfflineModeEnabled: StateFlow<Boolean> = _isOfflineModeEnabled.asStateFlow()

    private val _activeLocalModel = MutableStateFlow<LocalModelEntity?>(null)
    val activeLocalModel: StateFlow<LocalModelEntity?> = _activeLocalModel.asStateFlow()

    val localEngineState: StateFlow<LocalEngineState> = LocalLlmInferenceEngine.engineState
    val downloadStates: StateFlow<Map<String, DownloadProgressState>> = LocalModelDownloadManager.downloadStates

    private val _deviceHardwareSpecs = MutableStateFlow(DeviceHardwareAdvisor.getDeviceSpecs(application))
    val deviceHardwareSpecs: StateFlow<DeviceHardwareSpecs> = _deviceHardwareSpecs.asStateFlow()

    private val _lastLocalInferenceStats = MutableStateFlow<LocalInferenceStats?>(null)
    val lastLocalInferenceStats: StateFlow<LocalInferenceStats?> = _lastLocalInferenceStats.asStateFlow()

    // Loading & Generation State
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _currentRoutingDecision = MutableStateFlow<RoutingDecision?>(null)
    val currentRoutingDecision: StateFlow<RoutingDecision?> = _currentRoutingDecision.asStateFlow()

    // Live Intent Preview as user types
    private val _promptIntentPreview = MutableStateFlow<PromptIntentPreview?>(null)
    val promptIntentPreview: StateFlow<PromptIntentPreview?> = _promptIntentPreview.asStateFlow()

    // Duel / Multi-Model comparison state
    private val _duelState = MutableStateFlow(DuelResponseState())
    val duelState: StateFlow<DuelResponseState> = _duelState.asStateFlow()

    // Data streams from Repository
    val providers: StateFlow<List<ProviderEntity>> = repository.allProviders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val models: StateFlow<List<ModelConfigEntity>> = repository.allModels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val localModels: StateFlow<List<LocalModelEntity>> = repository.allLocalModels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routingRules: StateFlow<List<RoutingRuleEntity>> = repository.allRoutingRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentUsageLogs: StateFlow<List<UsageLogEntity>> = repository.recentUsageLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCost: StateFlow<Double?> = repository.totalCost
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalTokens: StateFlow<Int?> = repository.totalTokens
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalRequests: StateFlow<Int> = repository.totalRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val changeLogs: StateFlow<List<ChangeLogEntryEntity>> = repository.allChangeLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Provider Testing State
    private val _testingProviderId = MutableStateFlow<String?>(null)
    val testingProviderId: StateFlow<String?> = _testingProviderId.asStateFlow()

    init {
        viewModelScope.launch {
            if (repository.getProvidersList().isEmpty()) {
                AppDatabase.populateInitialData(db)
            }
            refreshHardwareSpecs()
            val active = repository.getActiveLoadedLocalModel()
            if (active != null) {
                _activeLocalModel.value = active
                LocalLlmInferenceEngine.loadModel(getApplication(), active)
            }
        }
    }

    fun refreshHardwareSpecs() {
        _deviceHardwareSpecs.value = DeviceHardwareAdvisor.getDeviceSpecs(getApplication())
    }

    fun toggleOfflineMode(enabled: Boolean) {
        _isOfflineModeEnabled.value = enabled
        if (enabled) {
            _qualityPreference.value = QualityPreference.LOCAL_ONLY
            viewModelScope.launch {
                repository.logChange(
                    category = ChangeLogCategory.ROUTING_RULE_CHANGE,
                    title = "Offline Mode Activated",
                    description = "All AI workloads routed exclusively to local on-device GGUF models.",
                    versionTag = "v1.1.0"
                )
            }
        }
    }

    fun onPromptTextChanged(text: String) {
        if (text.isBlank() || text.length < 3) {
            _promptIntentPreview.value = null
            return
        }

        if (_isOfflineModeEnabled.value || _qualityPreference.value == QualityPreference.LOCAL_ONLY) {
            val localTarget = _activeLocalModel.value ?: localModels.value.firstOrNull()
            _promptIntentPreview.value = PromptIntentPreview(
                detectedTask = TaskType.CASUAL_CHAT,
                complexityScore = 4,
                recommendedModelName = "${localTarget?.displayName ?: "Local GGUF"} [📱 Offline]",
                markers = listOf("Offline GGUF", "0ms Cloud Latency", "100% Private")
            )
            return
        }

        val currentProviders = providers.value
        val decision = ModelRouterEngine.resolveRoute(
            prompt = text,
            availableModels = models.value,
            routingRules = routingRules.value,
            globalQualityPreference = _qualityPreference.value,
            manualOverrideModel = _manualOverrideModel.value,
            providers = currentProviders
        )

        _promptIntentPreview.value = PromptIntentPreview(
            detectedTask = decision.taskType,
            complexityScore = decision.complexityScore,
            recommendedModelName = decision.selectedModel.displayName + if (decision.isModelOnline) " [🟢 Online]" else "",
            markers = decision.matchedMarkers
        )
    }

    fun setQualityPreference(pref: QualityPreference) {
        _qualityPreference.value = pref
        if (pref == QualityPreference.LOCAL_ONLY) {
            _isOfflineModeEnabled.value = true
        }
        viewModelScope.launch {
            repository.logChange(
                category = ChangeLogCategory.ROUTING_RULE_CHANGE,
                title = "Global Quality Preference Set: ${pref.displayName}",
                description = "Updated system-wide routing strategy to ${pref.name} (${pref.description})."
            )
        }
    }

    fun setManualOverrideModel(model: ModelConfigEntity?) {
        _manualOverrideModel.value = model
    }

    fun toggleDuelMode(enabled: Boolean, modelA: ModelConfigEntity? = null, modelB: ModelConfigEntity? = null) {
        _duelState.value = _duelState.value.copy(
            isDuelMode = enabled,
            modelA = modelA ?: models.value.find { it.tier == ModelTier.FLAGSHIP_FRONTIER } ?: models.value.firstOrNull(),
            modelB = modelB ?: models.value.find { it.tier == ModelTier.FAST_LIGHTWEIGHT } ?: models.value.getOrNull(1)
        )
    }

    fun sendPrompt(promptText: String) {
        val trimmed = promptText.trim()
        if (trimmed.isBlank() || _isGenerating.value) return

        val userMessageId = UUID.randomUUID().toString()
        val userMessage = ChatMessageEntity(
            id = userMessageId,
            sessionId = _currentSessionId.value,
            role = "user",
            content = trimmed,
            timestamp = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.insertMessage(userMessage)
            _isGenerating.value = true

            val isLocalRoute = _isOfflineModeEnabled.value ||
                    _qualityPreference.value == QualityPreference.LOCAL_ONLY ||
                    _manualOverrideModel.value?.providerId == "ollama"

            if (isLocalRoute) {
                // Execute on-device via LocalLlmInferenceEngine
                val activeModel = _activeLocalModel.value
                    ?: localModels.value.find { it.isDownloaded }
                    ?: localModels.value.firstOrNull()
                    ?: LocalModelEntity(
                        id = "qwen2.5-1.5b-q4",
                        displayName = "Qwen 2.5 1.5B Instruct",
                        fileName = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
                        modelFormat = "GGUF"
                    )

                val historyMessages = repository.getMessagesForSession(_currentSessionId.value)
                    .map { Pair(it.role, it.content) }

                val assistantMsgId = UUID.randomUUID().toString()
                var latestContent = ""
                var finalStats: LocalInferenceStats? = null

                LocalLlmInferenceEngine.streamLocalInference(
                    context = getApplication(),
                    prompt = trimmed,
                    history = historyMessages,
                    model = activeModel
                ).collect { (chunk, stats) ->
                    latestContent = chunk
                    finalStats = stats
                    _lastLocalInferenceStats.value = stats
                }

                val latency = finalStats?.totalTimeMs ?: 280L
                val tokensOut = finalStats?.completionTokens ?: (latestContent.length / 4)
                val tokensIn = finalStats?.promptTokens ?: (trimmed.length / 4)

                val assistantMessage = ChatMessageEntity(
                    id = assistantMsgId,
                    sessionId = _currentSessionId.value,
                    role = "assistant",
                    content = latestContent,
                    timestamp = System.currentTimeMillis(),
                    routedModelId = "local/${activeModel.fileName}",
                    routedModelName = "${activeModel.displayName} (Local GGUF)",
                    routedProviderId = "local_gguf",
                    taskTypeDetected = TaskType.CASUAL_CHAT,
                    routingReason = "📱 Processed 100% on-device via local GGUF engine (${activeModel.parameters}, ${finalStats?.tokensPerSecond ?: 24.5} tps).",
                    tokensPrompt = tokensIn,
                    tokensCompletion = tokensOut,
                    latencyMs = latency,
                    costUsd = 0.0,
                    isError = false
                )
                repository.insertMessage(assistantMessage)

                val usageLog = UsageLogEntity(
                    modelId = "local/${activeModel.fileName}",
                    modelName = "${activeModel.displayName} (Local GGUF)",
                    providerId = "local_gguf",
                    taskType = TaskType.CASUAL_CHAT,
                    promptTokens = tokensIn,
                    completionTokens = tokensOut,
                    costUsd = 0.0,
                    latencyMs = latency,
                    wasAutoRouted = true,
                    promptSnippet = trimmed.take(80)
                )
                repository.recordUsage(usageLog)
                _isGenerating.value = false
                return@launch
            }

            // Cloud Route
            val allAvailableModels = repository.getModelsList()
            val allRules = repository.getRoutingRulesList()
            val allProvidersList = repository.getProvidersList()

            // 1. Resolve Intelligent Route (strictly filtering for online models with active API keys)
            val routingDecision = ModelRouterEngine.resolveRoute(
                prompt = trimmed,
                availableModels = allAvailableModels,
                routingRules = allRules,
                globalQualityPreference = _qualityPreference.value,
                manualOverrideModel = _manualOverrideModel.value,
                providers = allProvidersList
            )
            _currentRoutingDecision.value = routingDecision

            val selectedModel = routingDecision.selectedModel
            val provider = allProvidersList.find { it.id == selectedModel.providerId }
                ?: ProviderEntity(
                    id = selectedModel.providerId,
                    name = selectedModel.providerId.replaceFirstChar { it.uppercase() },
                    baseUrl = AiApiService.getEndpointUrl(selectedModel.providerId)
                )

            // 2. Fetch recent conversation history
            val historyMessages = repository.getMessagesForSession(_currentSessionId.value)
                .map { Pair(it.role, it.content) }

            // 3. Execute AI Request
            val result = apiService.executePrompt(
                prompt = trimmed,
                history = historyMessages,
                model = selectedModel,
                provider = provider,
                taskType = routingDecision.taskType,
                fallbackProviders = allProvidersList
            )

            // 4. Save Assistant Response
            val assistantMsgId = UUID.randomUUID().toString()
            val assistantMessage = ChatMessageEntity(
                id = assistantMsgId,
                sessionId = _currentSessionId.value,
                role = "assistant",
                content = result.content,
                timestamp = System.currentTimeMillis(),
                routedModelId = selectedModel.id,
                routedModelName = selectedModel.displayName,
                routedProviderId = provider.id,
                taskTypeDetected = routingDecision.taskType,
                routingReason = routingDecision.reasoningText,
                tokensPrompt = result.tokensPrompt,
                tokensCompletion = result.tokensCompletion,
                latencyMs = result.latencyMs,
                costUsd = result.costUsd,
                isError = result.errorMessage != null
            )
            repository.insertMessage(assistantMessage)

            // 5. Record Usage Log
            val usageLog = UsageLogEntity(
                modelId = selectedModel.id,
                modelName = selectedModel.displayName,
                providerId = provider.id,
                taskType = routingDecision.taskType,
                promptTokens = result.tokensPrompt,
                completionTokens = result.tokensCompletion,
                costUsd = result.costUsd,
                latencyMs = result.latencyMs,
                wasAutoRouted = !routingDecision.wasOverridden,
                promptSnippet = trimmed.take(80)
            )
            repository.recordUsage(usageLog)

            _isGenerating.value = false
        }
    }

    // Local Model Management Actions
    fun downloadLocalModel(model: LocalModelEntity) {
        viewModelScope.launch {
            repository.updateLocalModelDownloadStatus(model.id, false, "", 0f, "DOWNLOADING")
            LocalModelDownloadManager.startDownload(
                context = getApplication(),
                model = model,
                onProgressUpdate = { progress, _, status ->
                    repository.updateLocalModelDownloadStatus(model.id, progress >= 1f, model.localFilePath, progress, status)
                },
                onComplete = { downloadedFile ->
                    repository.updateLocalModelDownloadStatus(
                        model.id,
                        isDownloaded = true,
                        path = downloadedFile.absolutePath,
                        progress = 1f,
                        status = "READY"
                    )
                    loadLocalModelToRam(model.copy(isDownloaded = true, localFilePath = downloadedFile.absolutePath))
                },
                onError = { error ->
                    repository.updateLocalModelDownloadStatus(model.id, false, "", 0f, "ERROR: $error")
                }
            )
        }
    }

    fun cancelModelDownload(modelId: String) {
        LocalModelDownloadManager.cancelDownload(modelId)
        viewModelScope.launch {
            repository.updateLocalModelDownloadStatus(modelId, false, "", 0f, "CANCELLED")
        }
    }

    fun loadLocalModelToRam(model: LocalModelEntity) {
        viewModelScope.launch {
            val res = LocalLlmInferenceEngine.loadModel(getApplication(), model)
            if (res.isSuccess) {
                repository.setLocalModelLoadedInMemory(model.id, true)
                _activeLocalModel.value = model.copy(isLoadedInMemory = true)
                refreshHardwareSpecs()
            }
        }
    }

    fun unloadLocalModelFromRam() {
        viewModelScope.launch {
            LocalLlmInferenceEngine.unloadModel()
            repository.unloadAllLocalModels()
            _activeLocalModel.value = null
            refreshHardwareSpecs()
        }
    }

    fun importLocalGgufUri(uri: Uri, name: String) {
        viewModelScope.launch {
            try {
                val (modelEntity, _) = LocalModelDownloadManager.importLocalFile(
                    context = getApplication(),
                    sourceUri = uri,
                    customName = name
                )
                repository.insertOrUpdateLocalModel(modelEntity)
                loadLocalModelToRam(modelEntity)
            } catch (_: Exception) {}
        }
    }

    fun addCustomGgufModel(
        displayName: String,
        downloadUrl: String,
        parameters: String,
        quantization: String,
        minRamGb: Double,
        contextLength: Int
    ) {
        viewModelScope.launch {
            val safeFileName = "${displayName.lowercase().replace(" ", "_")}_${quantization.lowercase()}.gguf"
            val id = "local_${System.currentTimeMillis()}"
            val model = LocalModelEntity(
                id = id,
                displayName = displayName,
                fileName = safeFileName,
                modelFormat = "GGUF",
                parameters = parameters,
                quantization = quantization,
                downloadUrl = downloadUrl,
                minRamRequiredGb = minRamGb,
                recommendedRamGb = minRamGb,
                contextLength = contextLength,
                fileSizeFormatted = "~2.5 GB",
                description = "Custom GGUF model configured via download URL / Google Drive link."
            )
            repository.insertOrUpdateLocalModel(model)
        }
    }

    fun updateLocalModelInferenceSettings(
        id: String,
        contextLength: Int,
        temperature: Float,
        topP: Float,
        gpuLayers: Int,
        cpuThreads: Int
    ) {
        viewModelScope.launch {
            repository.updateLocalModelInferenceConfig(id, contextLength, temperature, topP, gpuLayers, cpuThreads)
            val current = _activeLocalModel.value
            if (current?.id == id) {
                _activeLocalModel.value = current.copy(
                    contextLength = contextLength,
                    temperature = temperature,
                    topP = topP,
                    gpuLayers = gpuLayers,
                    cpuThreads = cpuThreads
                )
            }
        }
    }

    fun deleteLocalModel(id: String) {
        viewModelScope.launch {
            if (_activeLocalModel.value?.id == id) {
                unloadLocalModelFromRam()
            }
            repository.deleteLocalModel(id)
        }
    }

    // Provider & Cloud Actions
    fun updateProviderCredentials(providerId: String, apiKey: String, baseUrl: String) {
        viewModelScope.launch {
            val finalUrl = if (baseUrl.isBlank()) AiApiService.getEndpointUrl(providerId) else baseUrl
            repository.updateProviderCredentials(providerId, apiKey.trim(), finalUrl)
            val updatedProvider = repository.getProvidersList().find { it.id == providerId }
            if (updatedProvider != null && apiKey.isNotBlank()) {
                testProviderConnection(updatedProvider)
            }
        }
    }

    fun saveApiKey(providerId: String, apiKey: String) {
        viewModelScope.launch {
            val autoUrl = AiApiService.getEndpointUrl(providerId)
            repository.updateProviderCredentials(providerId, apiKey.trim(), autoUrl)
            repository.setProviderEnabled(providerId, true)
            
            // Auto test immediately
            val provider = repository.getProvidersList().find { it.id == providerId }
            if (provider != null) {
                testProviderConnection(provider)
            }
        }
    }

    fun setUniversalApiKey(apiKey: String) {
        val trimmed = apiKey.trim()
        if (trimmed.isBlank()) return
        viewModelScope.launch {
            val targetProviderId = AiApiService.detectProviderFromKey(trimmed)
            saveApiKey(targetProviderId, trimmed)
            // If it's an OpenRouter key, also ensure OpenRouter is set as active primary
            if (targetProviderId == "openrouter") {
                repository.setProviderEnabled("openrouter", true)
            }
        }
    }

    fun toggleProviderEnabled(providerId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setProviderEnabled(providerId, isEnabled)
        }
    }

    fun testProviderConnection(provider: ProviderEntity) {
        viewModelScope.launch {
            _testingProviderId.value = provider.id
            val (success, message) = apiService.testConnection(provider)
            val status = if (success) "Online: $message" else "Error: $message"
            repository.updateProviderTestStatus(provider.id, status)
            _testingProviderId.value = null
        }
    }

    fun addCustomProvider(name: String, baseUrl: String, apiKey: String) {
        viewModelScope.launch {
            val id = "custom_${System.currentTimeMillis()}"
            val provider = ProviderEntity(
                id = id,
                name = name,
                baseUrl = baseUrl,
                apiKey = apiKey,
                isEnabled = true,
                isCustom = true,
                statusMessage = "Custom provider added"
            )
            repository.insertProvider(provider)
        }
    }

    fun toggleModelEnabled(modelId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.setModelEnabled(modelId, isEnabled)
        }
    }

    fun addCustomModel(
        id: String,
        displayName: String,
        providerId: String,
        modelIdentifier: String,
        tier: ModelTier,
        capabilities: String,
        contextWindow: Int,
        costPer1MInput: Double,
        costPer1MOutput: Double
    ) {
        viewModelScope.launch {
            val model = ModelConfigEntity(
                id = id.ifBlank { "${providerId}/${modelIdentifier.replace('/', '_')}" },
                providerId = providerId,
                displayName = displayName,
                modelIdentifier = modelIdentifier,
                tier = tier,
                capabilities = capabilities,
                contextWindow = contextWindow,
                costPer1MInput = costPer1MInput,
                costPer1MOutput = costPer1MOutput,
                isEnabled = true
            )
            repository.insertModel(model)
        }
    }

    fun deleteModel(modelId: String) {
        viewModelScope.launch {
            repository.deleteModel(modelId)
        }
    }

    fun updateRoutingRule(rule: RoutingRuleEntity) {
        viewModelScope.launch {
            repository.updateRoutingRule(rule)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.deleteSession(_currentSessionId.value)
            val newSession = ChatSessionEntity(
                id = "session_${System.currentTimeMillis()}",
                title = "New Chat"
            )
            repository.insertSession(newSession)
            _currentSessionId.value = newSession.id
        }
    }

    fun clearAnalytics() {
        viewModelScope.launch {
            repository.clearUsageLogs()
        }
    }

    fun addCustomChangeLog(title: String, description: String, category: ChangeLogCategory) {
        viewModelScope.launch {
            repository.logChange(
                category = category,
                title = title,
                description = description,
                author = "User"
            )
        }
    }

    fun getExportableMarkdownLog(): String {
        return ChangeLogManager.generateMarkdown(changeLogs.value)
    }
}
