package com.example.data.model

enum class TaskType(val displayName: String, val description: String, val iconName: String) {
    CODING(
        displayName = "Coding & Debugging",
        description = "Code generation, refactoring, bug-fixing, algorithms, SQL",
        iconName = "code"
    ),
    REASONING(
        displayName = "Complex Reasoning",
        description = "Step-by-step logic, mathematical proofs, deep problem-solving",
        iconName = "psychology"
    ),
    RESEARCH(
        displayName = "Research & Analysis",
        description = "Literature review, comprehensive summaries, data synthesis",
        iconName = "menu_book"
    ),
    FAST_QUERY(
        displayName = "Quick Search & Lookup",
        description = "Fact-checking, definitions, quick answers, translations",
        iconName = "bolt"
    ),
    CASUAL_CHAT(
        displayName = "Casual Chat & Dialogue",
        description = "Conversational assistance, everyday questions, brainstorming",
        iconName = "forum"
    ),
    CREATIVE_WRITING(
        displayName = "Creative Writing",
        description = "Articles, stories, essays, marketing copy, poetry",
        iconName = "edit_note"
    )
}

enum class QualityPreference(val displayName: String, val description: String) {
    AUTO(
        displayName = "Auto (Smart Balance)",
        description = "Intelligently chooses model based on prompt complexity and task type"
    ),
    HIGH_QUALITY(
        displayName = "Maximum Quality (GPT-5 / Claude)",
        description = "Always route to top-tier flagship frontier models"
    ),
    BALANCED(
        displayName = "Balanced (Efficiency & Power)",
        description = "Optimal blend of response intelligence and speed"
    ),
    COST_SAVER(
        displayName = "Cost Saver / Free Tier",
        description = "Prioritize lightweight, low-cost or free models"
    ),
    LOCAL_ONLY(
        displayName = "Local Offline Only",
        description = "Route only to local Ollama / LM Studio instances"
    )
}

enum class ModelTier(val displayName: String, val badgeColorHex: Long) {
    FLAGSHIP_FRONTIER("Flagship (High Quality)", 0xFF8B5CF6),
    BALANCED("Balanced", 0xFF3B82F6),
    FAST_LIGHTWEIGHT("Fast & Low Cost", 0xFF10B981),
    LOCAL_OFFLINE("Local / Privacy", 0xFFF59E0B)
}

enum class ProviderType(val displayName: String, val defaultBaseUrl: String) {
    OPENROUTER("OpenRouter", "https://openrouter.ai/api/v1/"),
    OPENAI("OpenAI", "https://api.openai.com/v1/"),
    ANTHROPIC("Anthropic", "https://api.anthropic.com/v1/"),
    GOOGLE("Google AI (Gemini)", "https://generativelanguage.googleapis.com/v1beta/"),
    OLLAMA("Local Ollama", "http://10.0.2.2:11434/v1/"),
    CUSTOM("Custom OpenAI-Compatible", "http://localhost:8000/v1/")
}

enum class ChangeLogCategory(val displayName: String, val colorHex: Long) {
    MODEL_INTEGRATION("Model Integration", 0xFF8B5CF6),
    API_KEY_UPDATE("API Key / Config", 0xFF3B82F6),
    ROUTING_RULE_CHANGE("Routing Logic", 0xFF10B981),
    SYSTEM_FEATURE("Core Feature", 0xFFEC4899),
    USAGE_EVENT("Usage Event", 0xFFF59E0B)
}
