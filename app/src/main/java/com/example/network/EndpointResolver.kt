package com.example.network

/**
 * Intelligent Endpoint Discovery & Resolution Engine.
 * Automatically resolves and constructs correct API endpoints, protocols, and model identifiers
 * based solely on the user's API Key and provider type, eliminating the need for manual endpoint configuration.
 */
object EndpointResolver {

    data class ResolvedEndpoint(
        val providerId: String,
        val providerName: String,
        val baseUrl: String,
        val completionUrl: String,
        val authHeaderType: AuthType,
        val protocol: ApiProtocol,
        val isSelfDiscovered: Boolean = true
    )

    enum class AuthType { BEARER, QUERY_PARAM_KEY, X_API_KEY }
    enum class ApiProtocol { OPENAI_COMPATIBLE, GOOGLE_GEMINI, ANTHROPIC_MESSAGES }

    fun detectProviderFromKey(key: String): String {
        val trimmed = key.trim()
        return when {
            trimmed.startsWith("AIza", ignoreCase = false) -> "google"
            trimmed.startsWith("sk-ant-", ignoreCase = false) -> "anthropic"
            trimmed.startsWith("sk-or-", ignoreCase = false) -> "openrouter"
            trimmed.startsWith("gsk_", ignoreCase = false) -> "groq"
            trimmed.startsWith("deepseek-", ignoreCase = false) -> "deepseek"
            trimmed.startsWith("sk-proj-", ignoreCase = false) || trimmed.startsWith("sk-", ignoreCase = false) -> "openai"
            else -> "openrouter"
        }
    }

    fun resolveEndpoint(providerId: String, apiKey: String = ""): ResolvedEndpoint {
        val effectiveProviderId = if (apiKey.isNotBlank() && providerId == "custom") detectProviderFromKey(apiKey) else providerId.lowercase()
        return when (effectiveProviderId) {
            "google" -> ResolvedEndpoint("google", "Google AI Studio", "https://generativelanguage.googleapis.com/v1beta/", "https://generativelanguage.googleapis.com/v1beta/models/", AuthType.QUERY_PARAM_KEY, ApiProtocol.GOOGLE_GEMINI)
            "openai" -> ResolvedEndpoint("openai", "OpenAI", "https://api.openai.com/v1/", "https://api.openai.com/v1/chat/completions", AuthType.BEARER, ApiProtocol.OPENAI_COMPATIBLE)
            "anthropic" -> ResolvedEndpoint("anthropic", "Anthropic", "https://api.anthropic.com/v1/", "https://api.anthropic.com/v1/messages", AuthType.X_API_KEY, ApiProtocol.ANTHROPIC_MESSAGES)
            "openrouter" -> ResolvedEndpoint("openrouter", "OpenRouter Universal", "https://openrouter.ai/api/v1/", "https://openrouter.ai/api/v1/chat/completions", AuthType.BEARER, ApiProtocol.OPENAI_COMPATIBLE)
            "groq" -> ResolvedEndpoint("groq", "Groq High-Speed", "https://api.groq.com/openai/v1/", "https://api.groq.com/openai/v1/chat/completions", AuthType.BEARER, ApiProtocol.OPENAI_COMPATIBLE)
            "deepseek" -> ResolvedEndpoint("deepseek", "DeepSeek AI", "https://api.deepseek.com/v1/", "https://api.deepseek.com/v1/chat/completions", AuthType.BEARER, ApiProtocol.OPENAI_COMPATIBLE)
            "ollama" -> ResolvedEndpoint("ollama", "Ollama Local", "http://10.0.2.2:11434/v1/", "http://10.0.2.2:11434/v1/chat/completions", AuthType.BEARER, ApiProtocol.OPENAI_COMPATIBLE)
            else -> ResolvedEndpoint(effectiveProviderId, effectiveProviderId.replaceFirstChar { it.uppercase() }, "https://openrouter.ai/api/v1/", "https://openrouter.ai/api/v1/chat/completions", AuthType.BEARER, ApiProtocol.OPENAI_COMPATIBLE)
        }
    }

    fun resolveModelIdentifier(modelId: String, wireIdentifier: String, targetProviderId: String): String {
        val cleanIdentifier = wireIdentifier.substringAfter("/")
        return when (targetProviderId) {
            "openrouter" -> when {
                wireIdentifier.contains("/") -> wireIdentifier
                modelId.contains("/") -> modelId
                cleanIdentifier.startsWith("gemini") -> "google/$cleanIdentifier"
                cleanIdentifier.startsWith("claude") -> "anthropic/$cleanIdentifier"
                cleanIdentifier.startsWith("gpt") || cleanIdentifier.startsWith("o1") || cleanIdentifier.startsWith("o3") -> "openai/$cleanIdentifier"
                cleanIdentifier.startsWith("deepseek") -> "deepseek/$cleanIdentifier"
                cleanIdentifier.startsWith("llama") -> "meta-llama/$cleanIdentifier"
                else -> wireIdentifier
            }
            "google" -> when {
                cleanIdentifier.contains("pro") || cleanIdentifier.contains("opus") || cleanIdentifier.contains("sonnet") || cleanIdentifier.contains("o1") || cleanIdentifier.contains("o3") -> "gemini-3.1-pro-preview"
                cleanIdentifier.contains("flash-lite") || cleanIdentifier.contains("lite") -> "gemini-3.1-flash-lite-preview"
                cleanIdentifier.contains("flash") -> "gemini-3.5-flash"
                cleanIdentifier.startsWith("gemini-") -> cleanIdentifier
                else -> "gemini-3.5-flash"
            }
            "openai" -> when {
                cleanIdentifier.startsWith("o1") -> "o1"
                cleanIdentifier.startsWith("o3") -> "o3-mini"
                cleanIdentifier.contains("mini") || cleanIdentifier.contains("haiku") || cleanIdentifier.contains("flash") || cleanIdentifier.contains("8b") -> "gpt-4o-mini"
                cleanIdentifier.startsWith("gpt-") -> cleanIdentifier
                else -> "gpt-4o"
            }
            "anthropic" -> when {
                cleanIdentifier.contains("3-7") || cleanIdentifier.contains("3.7") -> "claude-3-7-sonnet-20250219"
                cleanIdentifier.contains("haiku") || cleanIdentifier.contains("mini") || cleanIdentifier.contains("flash") -> "claude-3-5-haiku-20241022"
                cleanIdentifier.contains("opus") -> "claude-3-opus-20240229"
                cleanIdentifier.startsWith("claude-") -> cleanIdentifier
                else -> "claude-3-5-sonnet-20241022"
            }
            "groq" -> when {
                cleanIdentifier.contains("r1") || cleanIdentifier.contains("reason") -> "deepseek-r1-distill-llama-70b"
                cleanIdentifier.contains("8b") || cleanIdentifier.contains("mini") || cleanIdentifier.contains("flash") -> "llama-3.1-8b-instant"
                cleanIdentifier.startsWith("llama-") || cleanIdentifier.startsWith("deepseek-") -> cleanIdentifier
                else -> "llama-3.3-70b-versatile"
            }
            "deepseek" -> if (cleanIdentifier.contains("r1") || cleanIdentifier.contains("reason")) "deepseek-reasoner" else "deepseek-chat"
            else -> cleanIdentifier
        }
    }
}
