package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ChangeLogCategory
import com.example.data.model.ChangeLogEntryEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSessionEntity
import com.example.data.model.ModelConfigEntity
import com.example.data.model.ProviderEntity
import com.example.data.model.RoutingRuleEntity
import com.example.data.model.TaskType
import com.example.data.model.UsageLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderDao {
    @Query("SELECT * FROM providers ORDER BY name ASC")
    fun getAllProvidersFlow(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers")
    suspend fun getAllProviders(): List<ProviderEntity>

    @Query("SELECT * FROM providers WHERE id = :id LIMIT 1")
    suspend fun getProviderById(id: String): ProviderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(provider: ProviderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(providers: List<ProviderEntity>)

    @Query("UPDATE providers SET apiKey = :apiKey, baseUrl = :baseUrl WHERE id = :id")
    suspend fun updateCredentials(id: String, apiKey: String, baseUrl: String)

    @Query("UPDATE providers SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setEnabled(id: String, isEnabled: Boolean)

    @Query("UPDATE providers SET lastTestedAt = :timestamp, statusMessage = :status WHERE id = :id")
    suspend fun updateTestStatus(id: String, timestamp: Long, status: String)

    @Query("DELETE FROM providers WHERE id = :id")
    suspend fun deleteProvider(id: String)
}

@Dao
interface ModelConfigDao {
    @Query("SELECT * FROM models ORDER BY displayName ASC")
    fun getAllModelsFlow(): Flow<List<ModelConfigEntity>>

    @Query("SELECT * FROM models WHERE isEnabled = 1")
    fun getEnabledModelsFlow(): Flow<List<ModelConfigEntity>>

    @Query("SELECT * FROM models")
    suspend fun getAllModels(): List<ModelConfigEntity>

    @Query("SELECT * FROM models WHERE id = :id LIMIT 1")
    suspend fun getModelById(id: String): ModelConfigEntity?

    @Query("SELECT * FROM models WHERE providerId = :providerId")
    suspend fun getModelsByProvider(providerId: String): List<ModelConfigEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(model: ModelConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(models: List<ModelConfigEntity>)

    @Query("UPDATE models SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun setEnabled(id: String, isEnabled: Boolean)

    @Query("DELETE FROM models WHERE id = :id")
    suspend fun deleteModel(id: String)
}

@Dao
interface RoutingRuleDao {
    @Query("SELECT * FROM routing_rules")
    fun getAllRulesFlow(): Flow<List<RoutingRuleEntity>>

    @Query("SELECT * FROM routing_rules")
    suspend fun getAllRules(): List<RoutingRuleEntity>

    @Query("SELECT * FROM routing_rules WHERE taskType = :taskType LIMIT 1")
    suspend fun getRuleForTask(taskType: TaskType): RoutingRuleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rule: RoutingRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<RoutingRuleEntity>)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
    fun getAllSessionsFlow(): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    @Update
    suspend fun updateSession(session: ChatSessionEntity)

    @Query("DELETE FROM chat_sessions WHERE id = :id")
    suspend fun deleteSession(id: String)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSessionFlow(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesForSession(sessionId: String): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)
}

@Dao
interface UsageLogDao {
    @Query("SELECT * FROM usage_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<UsageLogEntity>>

    @Query("SELECT * FROM usage_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogsFlow(limit: Int): Flow<List<UsageLogEntity>>

    @Query("SELECT SUM(costUsd) FROM usage_logs")
    fun getTotalCostFlow(): Flow<Double?>

    @Query("SELECT SUM(promptTokens + completionTokens) FROM usage_logs")
    fun getTotalTokensFlow(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM usage_logs")
    fun getTotalRequestsFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: UsageLogEntity)

    @Query("DELETE FROM usage_logs")
    suspend fun clearAllLogs()
}

@Dao
interface ChangeLogDao {
    @Query("SELECT * FROM change_logs ORDER BY timestamp DESC")
    fun getAllChangeLogsFlow(): Flow<List<ChangeLogEntryEntity>>

    @Query("SELECT * FROM change_logs WHERE category = :category ORDER BY timestamp DESC")
    fun getChangeLogsByCategoryFlow(category: ChangeLogCategory): Flow<List<ChangeLogEntryEntity>>

    @Query("SELECT * FROM change_logs ORDER BY timestamp DESC")
    suspend fun getAllChangeLogs(): List<ChangeLogEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ChangeLogEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<ChangeLogEntryEntity>)

    @Query("DELETE FROM change_logs WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface LocalModelDao {
    @Query("SELECT * FROM local_models ORDER BY displayName ASC")
    fun getAllLocalModelsFlow(): Flow<List<com.example.data.model.LocalModelEntity>>

    @Query("SELECT * FROM local_models")
    suspend fun getAllLocalModels(): List<com.example.data.model.LocalModelEntity>

    @Query("SELECT * FROM local_models WHERE isDownloaded = 1")
    suspend fun getDownloadedModels(): List<com.example.data.model.LocalModelEntity>

    @Query("SELECT * FROM local_models WHERE id = :id LIMIT 1")
    suspend fun getLocalModelById(id: String): com.example.data.model.LocalModelEntity?

    @Query("SELECT * FROM local_models WHERE isLoadedInMemory = 1 LIMIT 1")
    suspend fun getActiveLoadedModel(): com.example.data.model.LocalModelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(model: com.example.data.model.LocalModelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(models: List<com.example.data.model.LocalModelEntity>)

    @Query("UPDATE local_models SET isDownloaded = :isDownloaded, localFilePath = :path, downloadProgress = :progress, downloadStatus = :status WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean, path: String, progress: Float, status: String)

    @Query("UPDATE local_models SET isLoadedInMemory = :isLoaded WHERE id = :id")
    suspend fun setLoadedInMemory(id: String, isLoaded: Boolean)

    @Query("UPDATE local_models SET isLoadedInMemory = 0")
    suspend fun unloadAllModels()

    @Query("UPDATE local_models SET contextLength = :ctx, temperature = :temp, topP = :topP, gpuLayers = :gpu, cpuThreads = :threads WHERE id = :id")
    suspend fun updateInferenceConfig(id: String, ctx: Int, temp: Float, topP: Float, gpu: Int, threads: Int)

    @Query("DELETE FROM local_models WHERE id = :id")
    suspend fun deleteModel(id: String)
}
