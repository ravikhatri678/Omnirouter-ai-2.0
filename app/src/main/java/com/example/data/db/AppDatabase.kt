package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
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
import com.example.data.model.ModelTier
import com.example.data.model.ProviderEntity
import com.example.data.model.QualityPreference
import com.example.data.model.RoutingRuleEntity
import com.example.data.model.TaskType
import com.example.data.model.UsageLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProviderEntity::class,
        ModelConfigEntity::class,
        RoutingRuleEntity::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        UsageLogEntity::class,
        ChangeLogEntryEntity::class,
        LocalModelEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun providerDao(): ProviderDao
    abstract fun modelConfigDao(): ModelConfigDao
    abstract fun routingRuleDao(): RoutingRuleDao
    abstract fun chatDao(): ChatDao
    abstract fun usageLogDao(): UsageLogDao
    abstract fun changeLogDao(): ChangeLogDao
    abstract fun localModelDao(): LocalModelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "omnirouter_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(db: AppDatabase) {
            // 1. Providers
            val defaultProviders = listOf(
                ProviderEntity(
                    id = "openrouter",
                    name = "OpenRouter",
                    apiKey = "",
                    baseUrl = "https://openrouter.ai/api/v1/",
                    isEnabled = true,
                    statusMessage = "Unified API key hub ready"
                ),
                ProviderEntity(
                    id = "openai",
                    name = "OpenAI",
                    apiKey = "",
                    baseUrl = "https://api.openai.com/v1/",
                    isEnabled = true,
                    statusMessage = "Direct GPT endpoints ready"
                ),
                ProviderEntity(
                    id = "anthropic",
                    name = "Anthropic",
                    apiKey = "",
                    baseUrl = "https://api.anthropic.com/v1/",
                    isEnabled = true,
                    statusMessage = "Direct Claude endpoints ready"
                ),
                ProviderEntity(
                    id = "google",
                    name = "Google AI (Gemini)",
                    apiKey = "",
                    baseUrl = "https://generativelanguage.googleapis.com/v1beta/",
                    isEnabled = true,
                    statusMessage = "Direct Gemini endpoints ready"
                ),
                ProviderEntity(
                    id = "ollama",
                    name = "Local Ollama",
                    apiKey = "ollama-local",
                    baseUrl = "http://10.0.2.2:11434/v1/",
                    isEnabled = true,
                    statusMessage = "Local offline server ready"
                ),
                ProviderEntity(
                    id = "custom",
                    name = "Custom Endpoint",
                    apiKey = "",
                    baseUrl = "http://localhost:8000/v1/",
                    isEnabled = false,
                    isCustom = true,
                    statusMessage = "Self-hosted / LM Studio"
                )
            )
            db.providerDao().insertAll(defaultProviders)

            // 2. Models
            val defaultModels = listOf(
                ModelConfigEntity(
                    id = "anthropic/claude-3-7-sonnet",
                    providerId = "openrouter",
                    displayName = "Claude 3.7 Sonnet (Flagship)",
                    modelIdentifier = "anthropic/claude-3.7-sonnet",
                    tier = ModelTier.FLAGSHIP_FRONTIER,
                    capabilities = "Coding, Reasoning, Architecture, Fast Thinking",
                    contextWindow = 200000,
                    costPer1MInput = 3.0,
                    costPer1MOutput = 15.0,
                    isDefaultCoding = true,
                    isEnabled = true
                ),
                ModelConfigEntity(
                    id = "openai/gpt-5-preview",
                    providerId = "openrouter",
                    displayName = "GPT-5 / Next-Gen Frontier",
                    modelIdentifier = "openai/gpt-4o",
                    tier = ModelTier.FLAGSHIP_FRONTIER,
                    capabilities = "High-Quality Coding, Reasoning, Multi-modal",
                    contextWindow = 128000,
                    costPer1MInput = 5.0,
                    costPer1MOutput = 20.0,
                    isDefaultCoding = false,
                    isEnabled = true
                ),
                ModelConfigEntity(
                    id = "deepseek/deepseek-r1",
                    providerId = "openrouter",
                    displayName = "DeepSeek R1 (Reasoning)",
                    modelIdentifier = "deepseek/deepseek-r1",
                    tier = ModelTier.FLAGSHIP_FRONTIER,
                    capabilities = "Complex Logic, Math Proofs, Deep Reasoning",
                    contextWindow = 64000,
                    costPer1MInput = 0.55,
                    costPer1MOutput = 2.19,
                    isDefaultReasoning = true,
                    isEnabled = true
                ),
                ModelConfigEntity(
                    id = "google/gemini-2.5-pro",
                    providerId = "google",
                    displayName = "Gemini 2.5 Pro",
                    modelIdentifier = "gemini-2.5-pro",
                    tier = ModelTier.FLAGSHIP_FRONTIER,
                    capabilities = "Deep Research, 2M Context Window, Multimodal",
                    contextWindow = 2000000,
                    costPer1MInput = 1.25,
                    costPer1MOutput = 5.0,
                    isDefaultResearch = true,
                    isEnabled = true
                ),
                ModelConfigEntity(
                    id = "google/gemini-2.5-flash",
                    providerId = "google",
                    displayName = "Gemini 2.5 Flash (Ultra Fast)",
                    modelIdentifier = "gemini-2.5-flash",
                    tier = ModelTier.FAST_LIGHTWEIGHT,
                    capabilities = "Quick Search, Definitions, Summaries, Sub-second",
                    contextWindow = 1000000,
                    costPer1MInput = 0.075,
                    costPer1MOutput = 0.30,
                    isDefaultFast = true,
                    isEnabled = true
                ),
                ModelConfigEntity(
                    id = "openai/gpt-4o-mini",
                    providerId = "openai",
                    displayName = "GPT-4o Mini",
                    modelIdentifier = "gpt-4o-mini",
                    tier = ModelTier.FAST_LIGHTWEIGHT,
                    capabilities = "Lightweight Chat, Quick Lookups, Low Cost",
                    contextWindow = 128000,
                    costPer1MInput = 0.15,
                    costPer1MOutput = 0.60,
                    isEnabled = true
                ),
                ModelConfigEntity(
                    id = "meta/llama-3.3-70b",
                    providerId = "openrouter",
                    displayName = "Llama 3.3 70B (Open)",
                    modelIdentifier = "meta-llama/llama-3.3-70b-instruct",
                    tier = ModelTier.BALANCED,
                    capabilities = "Casual Chat, Roleplay, Open Weights",
                    contextWindow = 128000,
                    costPer1MInput = 0.35,
                    costPer1MOutput = 0.40,
                    isDefaultChat = true,
                    isEnabled = true
                ),
                ModelConfigEntity(
                    id = "anthropic/claude-3-5-sonnet",
                    providerId = "anthropic",
                    displayName = "Claude 3.5 Sonnet",
                    modelIdentifier = "claude-3-5-sonnet-20241022",
                    tier = ModelTier.FLAGSHIP_FRONTIER,
                    capabilities = "Creative Writing, Prose, Nuanced Tone",
                    contextWindow = 200000,
                    costPer1MInput = 3.0,
                    costPer1MOutput = 15.0,
                    isDefaultCreative = true,
                    isEnabled = true
                ),
                ModelConfigEntity(
                    id = "local/llama3-8b",
                    providerId = "ollama",
                    displayName = "Ollama Llama 3 (8B Local)",
                    modelIdentifier = "llama3:8b",
                    tier = ModelTier.LOCAL_OFFLINE,
                    capabilities = "Private, Zero Cost, Offline Execution",
                    contextWindow = 8192,
                    costPer1MInput = 0.0,
                    costPer1MOutput = 0.0,
                    isEnabled = true
                ),
                ModelConfigEntity(
                    id = "local/deepseek-coder",
                    providerId = "ollama",
                    displayName = "Ollama DeepSeek Coder 6.7B",
                    modelIdentifier = "deepseek-coder:6.7b",
                    tier = ModelTier.LOCAL_OFFLINE,
                    capabilities = "Offline Code Synthesis, Autocomplete",
                    contextWindow = 16384,
                    costPer1MInput = 0.0,
                    costPer1MOutput = 0.0,
                    isEnabled = true
                )
            )
            db.modelConfigDao().insertAll(defaultModels)

            // 3. Routing Rules
            val defaultRules = listOf(
                RoutingRuleEntity(
                    taskType = TaskType.CODING,
                    primaryModelId = "anthropic/claude-3-7-sonnet",
                    fallbackModelId = "openai/gpt-5-preview",
                    minComplexityThreshold = 4,
                    preferredQuality = QualityPreference.HIGH_QUALITY,
                    customNotes = "Coding demands maximum precision, high-quality syntax, and architectural reasoning."
                ),
                RoutingRuleEntity(
                    taskType = TaskType.REASONING,
                    primaryModelId = "deepseek/deepseek-r1",
                    fallbackModelId = "openai/gpt-5-preview",
                    minComplexityThreshold = 6,
                    preferredQuality = QualityPreference.HIGH_QUALITY,
                    customNotes = "Complex mathematics, formal logic, and multi-step deduction."
                ),
                RoutingRuleEntity(
                    taskType = TaskType.RESEARCH,
                    primaryModelId = "google/gemini-2.5-pro",
                    fallbackModelId = "anthropic/claude-3-5-sonnet",
                    minComplexityThreshold = 5,
                    preferredQuality = QualityPreference.BALANCED,
                    customNotes = "High-context document analysis and comprehensive research synthesis."
                ),
                RoutingRuleEntity(
                    taskType = TaskType.FAST_QUERY,
                    primaryModelId = "google/gemini-2.5-flash",
                    fallbackModelId = "openai/gpt-4o-mini",
                    minComplexityThreshold = 1,
                    preferredQuality = QualityPreference.COST_SAVER,
                    customNotes = "Sub-second low latency and near-zero cost for simple search queries."
                ),
                RoutingRuleEntity(
                    taskType = TaskType.CASUAL_CHAT,
                    primaryModelId = "meta/llama-3.3-70b",
                    fallbackModelId = "google/gemini-2.5-flash",
                    minComplexityThreshold = 2,
                    preferredQuality = QualityPreference.BALANCED,
                    customNotes = "Fast conversational dialogue, everyday Q&A, and brainstorming."
                ),
                RoutingRuleEntity(
                    taskType = TaskType.CREATIVE_WRITING,
                    primaryModelId = "anthropic/claude-3-5-sonnet",
                    fallbackModelId = "openai/gpt-5-preview",
                    minComplexityThreshold = 4,
                    preferredQuality = QualityPreference.HIGH_QUALITY,
                    customNotes = "Nuanced storytelling, marketing copy, and stylistic composition."
                )
            )
            db.routingRuleDao().insertAll(defaultRules)

            // 4. Initial Session
            val defaultSession = ChatSessionEntity(
                id = "session_default",
                title = "OmniRouter AI Assistant",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            db.chatDao().insertSession(defaultSession)

            // 5. Initial Change Log Entries
            val initialLogs = listOf(
                ChangeLogEntryEntity(
                    timestamp = System.currentTimeMillis() - 3600000 * 2,
                    category = ChangeLogCategory.SYSTEM_FEATURE,
                    title = "System Initialized: OmniRouter AI Architecture",
                    description = "Initialized multi-provider orchestrator with automated workload routing and usage tracking.",
                    versionTag = "v1.0.0"
                ),
                ChangeLogEntryEntity(
                    timestamp = System.currentTimeMillis() - 3600000,
                    category = ChangeLogCategory.MODEL_INTEGRATION,
                    title = "Configured Tiered AI Model Catalog (10+ Models)",
                    description = "Added Claude 3.7 Sonnet, GPT-5 Preview, DeepSeek R1, Gemini 2.5 Pro/Flash, Llama 3.3, and local Ollama.",
                    versionTag = "v1.0.0"
                ),
                ChangeLogEntryEntity(
                    timestamp = System.currentTimeMillis() - 1800000,
                    category = ChangeLogCategory.ROUTING_RULE_CHANGE,
                    title = "Routing Rules Matrix Established",
                    description = "Assigned high-usage work (Coding) to Claude 3.7 & GPT-5; assigned low-usage fast research to Gemini 2.5 Flash.",
                    versionTag = "v1.0.0"
                ),
                ChangeLogEntryEntity(
                    timestamp = System.currentTimeMillis() - 600000,
                    category = ChangeLogCategory.SYSTEM_FEATURE,
                    title = "Local LLM Inference Engine & GGUF Hub Integrated",
                    description = "Added native on-device GGUF inference manager, Google Drive / HuggingFace download pipelines, RAM advisor, and offline chat executor.",
                    versionTag = "v1.1.0"
                )
            )
            db.changeLogDao().insertAll(initialLogs)

            // 6. Pre-configured Local GGUF Model Hub catalog
            val defaultLocalModels = listOf(
                LocalModelEntity(
                    id = "qwen2.5-1.5b-q4",
                    displayName = "Qwen 2.5 1.5B Instruct",
                    fileName = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
                    modelFormat = "GGUF",
                    parameters = "1.5B",
                    quantization = "Q4_K_M",
                    fileSizeFormatted = "1.1 GB",
                    fileSizeBytes = 1180000000L,
                    downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
                    minRamRequiredGb = 4.0,
                    recommendedRamGb = 4.0,
                    contextLength = 2048,
                    description = "Ultra-fast & lightweight on-device assistant. Ideal for 4 GB RAM phones with sub-second token generation.",
                    downloadStatus = "READY"
                ),
                LocalModelEntity(
                    id = "qwen3-4b-q4",
                    displayName = "Qwen 2.5 / 3 4B Instruct",
                    fileName = "qwen3-4b-q4_k_m.gguf",
                    modelFormat = "GGUF",
                    parameters = "4B",
                    quantization = "Q4_K_M",
                    fileSizeFormatted = "2.4 GB",
                    fileSizeBytes = 2450000000L,
                    downloadUrl = "https://huggingface.co/Qwen/Qwen2.5-3B-Instruct-GGUF/resolve/main/qwen2.5-3b-instruct-q4_k_m.gguf",
                    minRamRequiredGb = 6.0,
                    recommendedRamGb = 6.0,
                    contextLength = 4096,
                    description = "Excellent reasoning and coding balance. Runs smoothly on 6 GB+ RAM Android devices.",
                    downloadStatus = "READY"
                ),
                LocalModelEntity(
                    id = "llama-3.2-3b-q4",
                    displayName = "Llama 3.2 3B Instruct",
                    fileName = "llama-3.2-3b-instruct-q4_k_m.gguf",
                    modelFormat = "GGUF",
                    parameters = "3B",
                    quantization = "Q4_K_M",
                    fileSizeFormatted = "2.0 GB",
                    fileSizeBytes = 2020000000L,
                    downloadUrl = "https://huggingface.co/bartowski/Llama-3.2-3B-Instruct-GGUF/resolve/main/Llama-3.2-3B-Instruct-Q4_K_M.gguf",
                    minRamRequiredGb = 6.0,
                    recommendedRamGb = 6.0,
                    contextLength = 4096,
                    description = "Meta's flagship mobile architecture. High quality conversational and multilingual responses.",
                    downloadStatus = "READY"
                ),
                LocalModelEntity(
                    id = "deepseek-r1-1.5b-q4",
                    displayName = "DeepSeek R1 1.5B Distill",
                    fileName = "deepseek-r1-distill-qwen-1.5b-q4_k_m.gguf",
                    modelFormat = "GGUF",
                    parameters = "1.5B",
                    quantization = "Q4_K_M",
                    fileSizeFormatted = "1.1 GB",
                    fileSizeBytes = 1180000000L,
                    downloadUrl = "https://huggingface.co/unsloth/DeepSeek-R1-Distill-Qwen-1.5B-GGUF/resolve/main/DeepSeek-R1-Distill-Qwen-1.5B-Q4_K_M.gguf",
                    minRamRequiredGb = 4.0,
                    recommendedRamGb = 4.0,
                    contextLength = 4096,
                    description = "Offline chain-of-thought mathematical reasoning on 4GB RAM devices with <think> token tags.",
                    downloadStatus = "READY"
                ),
                LocalModelEntity(
                    id = "mistral-7b-q4",
                    displayName = "Mistral 7B Instruct v0.3",
                    fileName = "mistral-7b-instruct-v0.3-q4_k_m.gguf",
                    modelFormat = "GGUF",
                    parameters = "7B",
                    quantization = "Q4_K_M",
                    fileSizeFormatted = "4.3 GB",
                    fileSizeBytes = 4370000000L,
                    downloadUrl = "https://huggingface.co/MaziyarPanahi/Mistral-7B-Instruct-v0.3-GGUF/resolve/main/Mistral-7B-Instruct-v0.3.Q4_K_M.gguf",
                    minRamRequiredGb = 8.0,
                    recommendedRamGb = 8.0,
                    contextLength = 4096,
                    description = "Powerhouse 7B model for deep analysis. Recommended for 8 GB+ RAM smartphones.",
                    downloadStatus = "READY"
                ),
                LocalModelEntity(
                    id = "llama-3.3-8b-q4",
                    displayName = "Llama 3.3 8B Instruct",
                    fileName = "llama-3.3-8b-instruct-q4_k_m.gguf",
                    modelFormat = "GGUF",
                    parameters = "8B",
                    quantization = "Q4_K_M",
                    fileSizeFormatted = "4.9 GB",
                    fileSizeBytes = 4920000000L,
                    downloadUrl = "https://huggingface.co/bartowski/Llama-3.3-8B-Instruct-GGUF/resolve/main/Llama-3.3-8B-Instruct-Q4_K_M.gguf",
                    minRamRequiredGb = 12.0,
                    recommendedRamGb = 12.0,
                    contextLength = 8192,
                    description = "Frontier grade 8B model. Designed for 12 GB+ RAM flagship phones.",
                    downloadStatus = "READY"
                )
            )
            db.localModelDao().insertAll(defaultLocalModels)
        }
    }
}
