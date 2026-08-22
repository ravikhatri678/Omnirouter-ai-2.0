package com.example.engine

import com.example.data.model.ModelConfigEntity
import com.example.data.model.ModelTier
import com.example.data.model.ProviderEntity
import com.example.data.model.QualityPreference
import com.example.data.model.RoutingRuleEntity
import com.example.data.model.TaskType

data class RoutingDecision(
    val taskType: TaskType,
    val complexityScore: Int, // 1 to 10
    val selectedModel: ModelConfigEntity,
    val reasoningText: String,
    val matchedMarkers: List<String>,
    val wasOverridden: Boolean = false,
    val isModelOnline: Boolean = false
)

object ModelRouterEngine {

    fun isModelOnline(model: ModelConfigEntity, providers: List<ProviderEntity>): Boolean {
        if (!model.isEnabled) return false
        val directProvider = providers.find { it.id == model.providerId }
        val openRouterProvider = providers.find { it.id == "openrouter" }

        val hasDirectKey = directProvider?.let {
            it.isEnabled && (it.apiKey.isNotBlank() || it.id == "ollama")
        } ?: false

        val hasOpenRouterKey = openRouterProvider?.let {
            it.isEnabled && it.apiKey.isNotBlank()
        } ?: false

        return hasDirectKey || hasOpenRouterKey
    }

    private val codingKeywords = setOf(
        "code", "coding", "program", "programming", "python", "kotlin", "java", "javascript",
        "typescript", "c++", "rust", "golang", "swift", "sql", "html", "css", "api", "bug",
        "fix", "error", "exception", "function", "class", "method", "refactor", "git", "regex",
        "json", "compiler", "algorithm", "data structure", "async", "coroutine", "database",
        "react", "compose", "gradle", "dependency", "sdk", "backend", "frontend"
    )

    private val codingCodeSymbols = listOf(
        "{", "}", "->", "=>", "fun ", "def ", "class ", "import ", "val ", "var ",
        "const ", "let ", "SELECT ", "public static", "void ", "const ", "return "
    )

    private val reasoningKeywords = setOf(
        "calculate", "prove", "proof", "deduce", "deduction", "step-by-step", "logic",
        "logical", "riddle", "puzzle", "math", "mathematics", "formula", "equation",
        "theorem", "probability", "derive", "paradox", "solve", "hypothesis", "premise",
        "algebra", "geometry", "calculus", "multi-step"
    )

    private val researchKeywords = setOf(
        "research", "literature", "analyze", "analysis", "paper", "summary", "summarize",
        "comprehensive", "compare and contrast", "history", "deep dive", "overview",
        "pros and cons", "study", "evidence", "findings", "implications", "citation",
        "explain in detail", "breakdown"
    )

    private val fastQueryStarters = listOf(
        "what is", "who was", "who is", "define", "definition of", "meaning of",
        "translate", "synonym", "antonym", "convert", "capital of", "when was",
        "where is", "spell", "how tall", "how far", "population of"
    )

    private val creativeKeywords = setOf(
        "story", "poem", "poetry", "essay", "narrative", "dialogue", "script",
        "song", "rhyme", "metaphor", "creative", "fiction", "character", "plot",
        "prose", "lyrics", "marketing copy", "slogan"
    )

    fun analyzePrompt(prompt: String): Pair<TaskType, List<String>> {
        val lower = prompt.lowercase().trim()
        val detectedMarkers = mutableListOf<String>()

        // 1. Check Coding
        var codingMatches = 0
        codingCodeSymbols.forEach { symbol ->
            if (prompt.contains(symbol)) {
                codingMatches += 2
                detectedMarkers.add("Code syntax: '$symbol'")
            }
        }
        val words = lower.split(Regex("[^a-zA-Z0-9_+#]")).filter { it.isNotBlank() }
        words.forEach { word ->
            if (codingKeywords.contains(word)) {
                codingMatches++
                detectedMarkers.add("Tech keyword: '$word'")
            }
        }
        if (codingMatches >= 2 || prompt.contains("```") || prompt.contains("fun ") || prompt.contains("def ")) {
            return Pair(TaskType.CODING, detectedMarkers)
        }

        // 2. Check Reasoning & Math
        var reasoningMatches = 0
        words.forEach { word ->
            if (reasoningKeywords.contains(word)) {
                reasoningMatches++
                detectedMarkers.add("Reasoning trigger: '$word'")
            }
        }
        if (reasoningMatches >= 2 || lower.contains("step by step") || lower.contains("solve for")) {
            return Pair(TaskType.REASONING, detectedMarkers)
        }

        // 3. Check Fast Query
        fastQueryStarters.forEach { starter ->
            if (lower.startsWith(starter) && lower.length < 80) {
                detectedMarkers.add("Fast lookup pattern: '$starter'")
                return Pair(TaskType.FAST_QUERY, detectedMarkers)
            }
        }

        // 4. Check Research & In-depth Analysis
        var researchMatches = 0
        words.forEach { word ->
            if (researchKeywords.contains(word)) {
                researchMatches++
                detectedMarkers.add("Research term: '$word'")
            }
        }
        if (researchMatches >= 2 || (lower.contains("compare") && lower.contains("and"))) {
            return Pair(TaskType.RESEARCH, detectedMarkers)
        }

        // 5. Check Creative Writing
        var creativeMatches = 0
        words.forEach { word ->
            if (creativeKeywords.contains(word)) {
                creativeMatches++
                detectedMarkers.add("Creative term: '$word'")
            }
        }
        if (creativeMatches >= 1 && (lower.contains("write a") || lower.contains("compose"))) {
            return Pair(TaskType.CREATIVE_WRITING, detectedMarkers)
        }

        // 6. Default to Casual Chat / General Dialogue
        detectedMarkers.add("General conversational dialogue")
        return Pair(TaskType.CASUAL_CHAT, detectedMarkers)
    }

    fun computeComplexityScore(prompt: String, taskType: TaskType, markersCount: Int): Int {
        var score = 3
        val charCount = prompt.trim().length

        when {
            charCount > 600 -> score += 3
            charCount > 250 -> score += 2
            charCount > 80 -> score += 1
            charCount < 40 -> score -= 1
        }

        when (taskType) {
            TaskType.CODING -> score += 3
            TaskType.REASONING -> score += 3
            TaskType.RESEARCH -> score += 2
            TaskType.CREATIVE_WRITING -> score += 1
            TaskType.FAST_QUERY -> score = (score - 2).coerceAtLeast(1)
            TaskType.CASUAL_CHAT -> score = (score - 1).coerceAtLeast(1)
        }

        if (markersCount >= 4) score += 1

        return score.coerceIn(1, 10)
    }

    fun resolveRoute(
        prompt: String,
        availableModels: List<ModelConfigEntity>,
        routingRules: List<RoutingRuleEntity>,
        globalQualityPreference: QualityPreference = QualityPreference.AUTO,
        manualOverrideModel: ModelConfigEntity? = null,
        providers: List<ProviderEntity> = emptyList()
    ): RoutingDecision {
        val (detectedTask, markers) = analyzePrompt(prompt)
        val complexity = computeComplexityScore(prompt, detectedTask, markers.size)

        // Handle manual override
        if (manualOverrideModel != null) {
            val isOnline = isModelOnline(manualOverrideModel, providers)
            return RoutingDecision(
                taskType = detectedTask,
                complexityScore = complexity,
                selectedModel = manualOverrideModel,
                reasoningText = if (isOnline) {
                    "User manually selected ${manualOverrideModel.displayName} [🟢 Online via API Key]."
                } else {
                    "User manually selected ${manualOverrideModel.displayName} (⚠️ Key not configured yet for ${manualOverrideModel.providerId})."
                },
                matchedMarkers = markers,
                wasOverridden = true,
                isModelOnline = isOnline
            )
        }

        val rule = routingRules.find { it.taskType == detectedTask }
        val effectiveQuality = if (globalQualityPreference != QualityPreference.AUTO) {
            globalQualityPreference
        } else {
            rule?.preferredQuality ?: QualityPreference.AUTO
        }

        // 1. First priority: Filter to models that are BOTH enabled AND confirmed ONLINE via user's API keys
        val activeModels = availableModels.filter { it.isEnabled }
        val onlineModels = activeModels.filter { isModelOnline(it, providers) }

        // If online models exist, strictly route only among the user's online models!
        val candidatePool = if (onlineModels.isNotEmpty()) onlineModels else activeModels
        val isCandidatePoolOnline = onlineModels.isNotEmpty()

        if (candidatePool.isEmpty()) {
            val fallback = availableModels.firstOrNull() ?: ModelConfigEntity(
                id = "google/gemini-2.5-flash",
                providerId = "google",
                displayName = "Gemini 2.5 Flash",
                modelIdentifier = "gemini-2.5-flash",
                tier = ModelTier.FAST_LIGHTWEIGHT,
                capabilities = "Fast Search",
                isEnabled = true
            )
            return RoutingDecision(
                taskType = detectedTask,
                complexityScore = complexity,
                selectedModel = fallback,
                reasoningText = "Default fallback applied. No online models found.",
                matchedMarkers = markers,
                isModelOnline = false
            )
        }

        // Select model based on effective quality & task from the candidatePool
        val selectedModel = when (effectiveQuality) {
            QualityPreference.LOCAL_ONLY -> {
                candidatePool.find { it.tier == ModelTier.LOCAL_OFFLINE }
                    ?: candidatePool.find { it.providerId == "ollama" }
                    ?: candidatePool.first()
            }
            QualityPreference.COST_SAVER -> {
                candidatePool.find { it.tier == ModelTier.FAST_LIGHTWEIGHT }
                    ?: candidatePool.minByOrNull { it.costPer1MInput }
                    ?: candidatePool.first()
            }
            QualityPreference.HIGH_QUALITY -> {
                rule?.primaryModelId?.let { id -> candidatePool.find { it.id == id } }
                    ?: candidatePool.find { it.tier == ModelTier.FLAGSHIP_FRONTIER }
                    ?: candidatePool.first()
            }
            QualityPreference.BALANCED -> {
                rule?.primaryModelId?.let { id -> candidatePool.find { it.id == id } }
                    ?: candidatePool.find { it.tier == ModelTier.BALANCED }
                    ?: candidatePool.find { it.tier == ModelTier.FLAGSHIP_FRONTIER }
                    ?: candidatePool.first()
            }
            QualityPreference.AUTO -> {
                if (complexity >= 6 || detectedTask == TaskType.CODING || detectedTask == TaskType.REASONING) {
                    rule?.primaryModelId?.let { id -> candidatePool.find { it.id == id } }
                        ?: candidatePool.find { it.tier == ModelTier.FLAGSHIP_FRONTIER }
                        ?: candidatePool.first()
                } else if (complexity <= 3 || detectedTask == TaskType.FAST_QUERY) {
                    candidatePool.find { it.isDefaultFast }
                        ?: candidatePool.find { it.tier == ModelTier.FAST_LIGHTWEIGHT }
                        ?: candidatePool.first()
                } else {
                    rule?.primaryModelId?.let { id -> candidatePool.find { it.id == id } }
                        ?: candidatePool.first()
                }
            }
        }

        val reasoningExplanation = buildString {
            append("Classified as [${detectedTask.displayName}] (Score: $complexity/10). ")
            if (isCandidatePoolOnline) {
                append("Routed to active online model ${selectedModel.displayName} (${selectedModel.providerId.uppercase()}). ")
            } else {
                append("Routed to ${selectedModel.displayName} (⚠️ Add API key in settings to activate live provider). ")
            }
            if (markers.isNotEmpty()) {
                append("Markers: ${markers.take(2).joinToString(", ")}. ")
            }
            when {
                detectedTask == TaskType.CODING -> {
                    append("Prioritized precision & syntax correctness.")
                }
                detectedTask == TaskType.REASONING -> {
                    append("Optimized for deductive chain-of-thought analysis.")
                }
                detectedTask == TaskType.RESEARCH -> {
                    append("Allocated high-context synthesis window.")
                }
                detectedTask == TaskType.FAST_QUERY -> {
                    append("Routed to ultra-fast low-latency tier.")
                }
                else -> {
                    append("Optimized under ${effectiveQuality.displayName} policy.")
                }
            }
        }

        return RoutingDecision(
            taskType = detectedTask,
            complexityScore = complexity,
            selectedModel = selectedModel,
            reasoningText = reasoningExplanation,
            matchedMarkers = markers,
            isModelOnline = isCandidatePoolOnline && isModelOnline(selectedModel, providers)
        )
    }
}
