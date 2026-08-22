package com.example.data.repository

import com.example.data.dao.ChangeLogDao
import com.example.data.dao.ChatDao
import com.example.data.dao.LocalModelDao
import com.example.data.dao.ModelConfigDao
import com.example.data.dao.ProviderDao
import com.example.data.dao.RoutingRuleDao
import com.example.data.dao.UsageLogDao
import com.example.data.model.ChangeLogCategory
import com.example.data.model.ChangeLogEntryEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSessionEntity
import com.example.data.model.LocalModelEntity
import com.example.data.model.ModelConfigEntity
import com.example.data.model.ProviderEntity
import com.example.data.model.RoutingRuleEntity
import com.example.data.model.TaskType
import com.example.data.model.UsageLogEntity
import kotlinx.coroutines.flow.Flow

class OmniRepository(
    private val providerDao: ProviderDao,
    private val modelConfigDao: ModelConfigDao,
    private val routingRuleDao: RoutingRuleDao,
    private val chatDao: ChatDao,
    private val usageLogDao: UsageLogDao,
    private val changeLogDao: ChangeLogDao,
    private val localModelDao: LocalModelDao
) {
    // Providers
    val allProviders: Flow<List<ProviderEntity>> = providerDao.getAllProvidersFlow()
    suspend fun getProvidersList(): List<ProviderEntity> = providerDao.getAllProviders()
    suspend fun getProviderById(id: String): ProviderEntity? = providerDao.getProviderById(id)
    suspend fun updateProviderCredentials(id: String, apiKey: String, baseUrl: String) {
        providerDao.updateCredentials(id, apiKey, baseUrl)
        logChange(
            category = ChangeLogCategory.API_KEY_UPDATE,
            title = "API Credentials Updated: $id",
            description = "Updated API key / endpoint baseUrl for provider $id."
        )
    }
    suspend fun setProviderEnabled(id: String, isEnabled: Boolean) {
        providerDao.setEnabled(id, isEnabled)
        logChange(
            category = ChangeLogCategory.API_KEY_UPDATE,
            title = "Provider Status Toggled: $id",
            description = "Provider $id set to ${if (isEnabled) "Active" else "Disabled"}."
        )
    }
    suspend fun updateProviderTestStatus(id: String, status: String) {
        providerDao.updateTestStatus(id, System.currentTimeMillis(), status)
    }
    suspend fun insertProvider(provider: ProviderEntity) {
        providerDao.insertOrUpdate(provider)
        logChange(
            category = ChangeLogCategory.API_KEY_UPDATE,
            title = "Custom Provider Added: ${provider.name}",
            description = "Registered custom provider ${provider.name} (${provider.baseUrl})."
        )
    }

    // Models
    val allModels: Flow<List<ModelConfigEntity>> = modelConfigDao.getAllModelsFlow()
    val enabledModels: Flow<List<ModelConfigEntity>> = modelConfigDao.getEnabledModelsFlow()
    suspend fun getModelsList(): List<ModelConfigEntity> = modelConfigDao.getAllModels()
    suspend fun getModelById(id: String): ModelConfigEntity? = modelConfigDao.getModelById(id)
    suspend fun insertModel(model: ModelConfigEntity) {
        modelConfigDao.insertOrUpdate(model)
        logChange(
            category = ChangeLogCategory.MODEL_INTEGRATION,
            title = "Model Config Saved: ${model.displayName}",
            description = "Model ${model.displayName} (${model.id}) configured for provider ${model.providerId}."
        )
    }
    suspend fun setModelEnabled(id: String, isEnabled: Boolean) {
        modelConfigDao.setEnabled(id, isEnabled)
        logChange(
            category = ChangeLogCategory.MODEL_INTEGRATION,
            title = "Model Availability Changed: $id",
            description = "Model $id ${if (isEnabled) "enabled" else "disabled"}."
        )
    }
    suspend fun deleteModel(id: String) {
        modelConfigDao.deleteModel(id)
        logChange(
            category = ChangeLogCategory.MODEL_INTEGRATION,
            title = "Model Removed: $id",
            description = "Removed model $id from active configuration catalog."
        )
    }

    // Routing Rules
    val allRoutingRules: Flow<List<RoutingRuleEntity>> = routingRuleDao.getAllRulesFlow()
    suspend fun getRoutingRulesList(): List<RoutingRuleEntity> = routingRuleDao.getAllRules()
    suspend fun getRuleForTask(taskType: TaskType): RoutingRuleEntity? = routingRuleDao.getRuleForTask(taskType)
    suspend fun updateRoutingRule(rule: RoutingRuleEntity) {
        routingRuleDao.insertOrUpdate(rule)
        logChange(
            category = ChangeLogCategory.ROUTING_RULE_CHANGE,
            title = "Routing Rule Updated: ${rule.taskType.displayName}",
            description = "Mapped ${rule.taskType.name} -> Primary: ${rule.primaryModelId} (Quality: ${rule.preferredQuality.name})."
        )
    }

    // Chat
    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessionsFlow()
    fun getMessagesForSessionFlow(sessionId: String): Flow<List<ChatMessageEntity>> =
        chatDao.getMessagesForSessionFlow(sessionId)
    suspend fun getMessagesForSession(sessionId: String): List<ChatMessageEntity> =
        chatDao.getMessagesForSession(sessionId)
    suspend fun insertSession(session: ChatSessionEntity) = chatDao.insertSession(session)
    suspend fun insertMessage(message: ChatMessageEntity) = chatDao.insertMessage(message)
    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteMessagesForSession(sessionId)
        chatDao.deleteSession(sessionId)
    }

    // Usage & Analytics
    val allUsageLogs: Flow<List<UsageLogEntity>> = usageLogDao.getAllLogsFlow()
    val recentUsageLogs: Flow<List<UsageLogEntity>> = usageLogDao.getRecentLogsFlow(30)
    val totalCost: Flow<Double?> = usageLogDao.getTotalCostFlow()
    val totalTokens: Flow<Int?> = usageLogDao.getTotalTokensFlow()
    val totalRequests: Flow<Int> = usageLogDao.getTotalRequestsFlow()
    suspend fun recordUsage(log: UsageLogEntity) = usageLogDao.insertLog(log)
    suspend fun clearUsageLogs() = usageLogDao.clearAllLogs()

    // Change Logs
    val allChangeLogs: Flow<List<ChangeLogEntryEntity>> = changeLogDao.getAllChangeLogsFlow()
    fun getChangeLogsByCategory(category: ChangeLogCategory): Flow<List<ChangeLogEntryEntity>> =
        changeLogDao.getChangeLogsByCategoryFlow(category)
    suspend fun getAllChangeLogsList(): List<ChangeLogEntryEntity> = changeLogDao.getAllChangeLogs()
    suspend fun logChange(
        category: ChangeLogCategory,
        title: String,
        description: String,
        versionTag: String = "v1.0.0",
        author: String = "System"
    ) {
        changeLogDao.insert(
            ChangeLogEntryEntity(
                category = category,
                title = title,
                description = description,
                versionTag = versionTag,
                author = author
            )
        )
    }

    // Local LLM & GGUF Models
    val allLocalModels: Flow<List<LocalModelEntity>> = localModelDao.getAllLocalModelsFlow()
    suspend fun getAllLocalModelsList(): List<LocalModelEntity> = localModelDao.getAllLocalModels()
    suspend fun getDownloadedLocalModelsList(): List<LocalModelEntity> = localModelDao.getDownloadedModels()
    suspend fun getLocalModelById(id: String): LocalModelEntity? = localModelDao.getLocalModelById(id)
    suspend fun getActiveLoadedLocalModel(): LocalModelEntity? = localModelDao.getActiveLoadedModel()

    suspend fun insertOrUpdateLocalModel(model: LocalModelEntity) {
        localModelDao.insertOrUpdate(model)
        logChange(
            category = ChangeLogCategory.MODEL_INTEGRATION,
            title = "Local GGUF Configured: ${model.displayName}",
            description = "Configured local on-device model ${model.displayName} (${model.parameters}, ${model.quantization}).",
            versionTag = "v1.1.0"
        )
    }

    suspend fun updateLocalModelDownloadStatus(id: String, isDownloaded: Boolean, path: String, progress: Float, status: String) {
        localModelDao.updateDownloadStatus(id, isDownloaded, path, progress, status)
    }

    suspend fun setLocalModelLoadedInMemory(id: String, isLoaded: Boolean) {
        if (isLoaded) {
            localModelDao.unloadAllModels()
        }
        localModelDao.setLoadedInMemory(id, isLoaded)
    }

    suspend fun unloadAllLocalModels() {
        localModelDao.unloadAllModels()
    }

    suspend fun updateLocalModelInferenceConfig(id: String, ctx: Int, temp: Float, topP: Float, gpu: Int, threads: Int) {
        localModelDao.updateInferenceConfig(id, ctx, temp, topP, gpu, threads)
    }

    suspend fun deleteLocalModel(id: String) {
        localModelDao.deleteModel(id)
        logChange(
            category = ChangeLogCategory.MODEL_INTEGRATION,
            title = "Local Model Removed: $id",
            description = "Removed local model $id from on-device manager.",
            versionTag = "v1.1.0"
        )
    }
}
